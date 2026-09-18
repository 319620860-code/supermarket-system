package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.StockInRecordMapper;
import com.supermarket.backend.mapper.SupplierMapper;
import com.supermarket.backend.dto.StockInItemLineVO;
import com.supermarket.backend.entity.StockInRecord;
import com.supermarket.backend.entity.Supplier;
import com.supermarket.backend.service.StockInRecordService;
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
public class StockInRecordServiceImpl implements StockInRecordService {

    @Autowired
    private StockInRecordMapper stockInRecordMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public StockInRecord getById(Long id) {
        return stockInRecordMapper.selectById(id);
    }

    @Override
    public StockInRecord getByBatchNumber(String batchNumber) {
        return stockInRecordMapper.selectByBatchNumber(batchNumber);
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

        List<StockInRecord> rows = stockInRecordMapper.selectPage(params);
        int total = stockInRecordMapper.selectCount(params);

        return PagePayload.of(rows, total, page, pageSize);
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
        List<StockInItemLineVO> rows = stockInRecordMapper.selectItemLinesPage(params);
        int total = stockInRecordMapper.selectItemLinesCount();
        return PagePayload.of(rows, total, page, pageSize);
    }

    @Override
    @Transactional
    public StockInRecord create(StockInRecord stockInRecord) {
        if (stockInRecord.getProductId() == null || stockInRecord.getWarehouseId() == null) {
            throw new IllegalArgumentException("商品与仓库不能为空");
        }
        if (stockInRecord.getSupplierId() == null) {
            throw new IllegalArgumentException("请选择供应商");
        }
        if (stockInRecord.getQuantity() == null || stockInRecord.getQuantity() <= 0) {
            throw new IllegalArgumentException("入库数量必须大于0");
        }
        if (stockInRecord.getUnitPrice() == null || stockInRecord.getUnitPrice() <= 0) {
            throw new IllegalArgumentException("入库单价必须大于0");
        }
        Supplier supplier = supplierMapper.selectById(stockInRecord.getSupplierId());
        if (supplier == null) {
            throw new IllegalArgumentException("供应商不存在");
        }
        if (supplier.getStatus() == null || supplier.getStatus() != 1) {
            throw new IllegalArgumentException("供应商已停用，不能入库");
        }

        // 生成批次号
        String batchNumber = "IN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        stockInRecord.setBatchNumber(batchNumber);
        
        // 计算总价
        Double totalPrice = stockInRecord.getUnitPrice() * stockInRecord.getQuantity();
        stockInRecord.setTotalPrice(totalPrice);
        
        // 设置默认值
        stockInRecord.setStatus(1);
        stockInRecord.setCreateTime(new Date());
        stockInRecord.setUpdateTime(new Date());
        stockInRecord.setOperatorId(SecurityUtils.currentUserIdOrNull());

        // 插入入库记录
        stockInRecordMapper.insert(stockInRecord);
        
        // 更新库存
        inventoryService.updateQuantity(stockInRecord.getProductId(), stockInRecord.getWarehouseId(), stockInRecord.getQuantity());
        
        return stockInRecord;
    }

    @Override
    public StockInRecord update(StockInRecord stockInRecord) {
        stockInRecord.setUpdateTime(new Date());
        stockInRecordMapper.update(stockInRecord);
        return stockInRecord;
    }

    @Override
    public boolean delete(Long id) {
        return stockInRecordMapper.delete(id) > 0;
    }

    @Override
    public List<StockInRecord> getByProductId(Long productId) {
        return stockInRecordMapper.selectByProductId(productId);
    }
}
