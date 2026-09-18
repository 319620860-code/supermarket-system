package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.StockCheckRecordMapper;
import com.supermarket.backend.entity.StockCheckRecord;
import com.supermarket.backend.entity.Inventory;
import com.supermarket.backend.entity.Product;
import com.supermarket.backend.service.StockCheckRecordService;
import com.supermarket.backend.service.ProductService;
import com.supermarket.backend.util.PagePayload;
import com.supermarket.backend.service.InventoryService;
import com.supermarket.backend.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StockCheckRecordServiceImpl implements StockCheckRecordService {

    @Autowired
    private StockCheckRecordMapper stockCheckRecordMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public Map<String, Object> getById(Long id) {
        return stockCheckRecordMapper.selectById(id);
    }

    @Override
    public StockCheckRecord getByCheckNumber(String checkNumber) {
        return stockCheckRecordMapper.selectByCheckNumber(checkNumber);
    }

    @Override
    public Map<String, Object> getPage(String keyword, Long productId, Integer status, String startDate, String endDate, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("productId", productId);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("offset", (page - 1) * pageSize);
        params.put("pageSize", pageSize);

        List<StockCheckRecord> rows = stockCheckRecordMapper.selectPage(params);
        int total = stockCheckRecordMapper.selectCount(params);

        return PagePayload.of(rows, total, page, pageSize);
    }

    @Override
    public StockCheckRecord create(StockCheckRecord stockCheckRecord) {
        // 生成盘点单号
        String checkNumber = "CHECK" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        stockCheckRecord.setCheckNumber(checkNumber);
        
        // 计算差异
        Integer difference = stockCheckRecord.getActualQuantity() - stockCheckRecord.getSystemQuantity();
        Double differenceAmount = difference * stockCheckRecord.getUnitPrice();
        stockCheckRecord.setDifference(difference);
        stockCheckRecord.setDifferenceAmount(differenceAmount);
        
        // 设置默认值
        stockCheckRecord.setStatus(0); // 0: 待确认 1: 已确认
        stockCheckRecord.setCreateTime(new Date());
        stockCheckRecord.setUpdateTime(new Date());
        stockCheckRecord.setOperatorId(SecurityUtils.currentUserIdOrNull());
        
        // 插入盘点记录
        stockCheckRecordMapper.insert(stockCheckRecord);
        
        return stockCheckRecord;
    }

    @Override
    public StockCheckRecord update(StockCheckRecord stockCheckRecord) {
        // 重新计算差异
        Integer difference = stockCheckRecord.getActualQuantity() - stockCheckRecord.getSystemQuantity();
        Double differenceAmount = difference * stockCheckRecord.getUnitPrice();
        stockCheckRecord.setDifference(difference);
        stockCheckRecord.setDifferenceAmount(differenceAmount);
        
        stockCheckRecord.setUpdateTime(new Date());
        stockCheckRecordMapper.update(stockCheckRecord);
        return stockCheckRecord;
    }

    @Override
    public boolean delete(Long id) {
        return stockCheckRecordMapper.delete(id) > 0;
    }

    @Override
    public List<StockCheckRecord> getByProductId(Long productId) {
        return stockCheckRecordMapper.selectByProductId(productId);
    }

    @Override
    public Map<String, Object> getItemLinePage(Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("offset", (page - 1) * pageSize);
        params.put("pageSize", pageSize);
        List<Map<String, Object>> rows = stockCheckRecordMapper.selectItemLinesPage(params);
        int total = stockCheckRecordMapper.selectItemLinesCount();
        return PagePayload.of(rows, total, page, pageSize);
    }

    @Override
    @Transactional
    public Map<String, Object> confirmCheck(Long id) {
        // 获取盘点记录
        Map<String, Object> stockCheckRecordMap = stockCheckRecordMapper.selectById(id);
        if (stockCheckRecordMap == null) {
            throw new RuntimeException("盘点记录不存在");
        }

        // 从Map中获取需要的字段
        Long productId = (Long) stockCheckRecordMap.get("productId");
        Integer actualQuantity = (Integer) stockCheckRecordMap.get("actualQuantity");
        Integer systemQuantity = (Integer) stockCheckRecordMap.get("systemQuantity");
        Integer difference = (Integer) stockCheckRecordMap.get("difference");

        // 创建临时StockCheckRecord对象用于计算
        StockCheckRecord tempRecord = new StockCheckRecord();
        tempRecord.setProductId(productId);
        tempRecord.setActualQuantity(actualQuantity);
        tempRecord.setSystemQuantity(systemQuantity);
        tempRecord.setDifference(difference);

        List<Inventory> invRows = inventoryService.listByProductId(productId);
        int newQuantity = resolveQuantityAfterConfirm(tempRecord, invRows, productService.getById(productId));

        // 更新盘点记录状态
        StockCheckRecord updateRecord = new StockCheckRecord();
        updateRecord.setId(id);
        updateRecord.setStatus(1);
        updateRecord.setUpdateTime(new Date());
        stockCheckRecordMapper.update(updateRecord);

        productService.updateStock(productId, newQuantity);
        inventoryService.syncSingleWarehouseInventoryIfPresent(productId, newQuantity);

        // 重新获取更新后的记录
        return stockCheckRecordMapper.selectById(id);
    }

    /**
     * 单仓或无分仓库存行：商品总库存 = 实盘数量（或系统+差异）。<br>
     * 多仓：商品总库存 = 当前商品表数量 + 本行差异（避免用某一仓实盘覆盖全局数量）。
     */
    private static int resolveQuantityAfterConfirm(StockCheckRecord r, List<Inventory> invRows, Product product) {
        int invCount = invRows == null ? 0 : invRows.size();
        if (invCount > 1) {
            if (r.getDifference() == null) {
                throw new IllegalStateException("多仓库场景下盘点记录需包含差异数量");
            }
            int cur = product != null && product.getQuantity() != null ? product.getQuantity() : 0;
            int next = cur + r.getDifference();
            if (next < 0) {
                throw new IllegalStateException("调整后商品库存不能为负");
            }
            return next;
        }
        return resolveAbsoluteQuantityAfterCheck(r);
    }

    /**
     * 实盘数量优先；若缺失则用 系统数量 + 差异 推导。
     */
    private static int resolveAbsoluteQuantityAfterCheck(StockCheckRecord r) {
        if (r.getActualQuantity() != null) {
            return r.getActualQuantity();
        }
        if (r.getSystemQuantity() != null && r.getDifference() != null) {
            return r.getSystemQuantity() + r.getDifference();
        }
        throw new IllegalStateException("盘点记录缺少实盘数量，无法确认");
    }
}
