package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.StockOutRecordMapper;
import com.supermarket.backend.mapper.OrderMapper;
import com.supermarket.backend.mapper.ProductMapper;
import com.supermarket.backend.dto.StockOutItemLineVO;
import com.supermarket.backend.entity.StockOutRecord;
import com.supermarket.backend.entity.Order;
import com.supermarket.backend.entity.Product;
import com.supermarket.backend.service.StockOutRecordService;
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
public class StockOutRecordServiceImpl implements StockOutRecordService {

    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public StockOutRecord getById(Long id) {
        return stockOutRecordMapper.selectById(id);
    }

    @Override
    public StockOutRecord getByBatchNumber(String batchNumber) {
        return stockOutRecordMapper.selectByBatchNumber(batchNumber);
    }

    @Override
    public Map<String, Object> getPage(String keyword, Long productId, String outType, Integer status, String startDate, String endDate, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("productId", productId);
        params.put("outType", outType);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("offset", (page - 1) * pageSize);
        params.put("pageSize", pageSize);

        List<StockOutRecord> rows = stockOutRecordMapper.selectPage(params);
        int total = stockOutRecordMapper.selectCount(params);

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
        List<StockOutItemLineVO> rows = stockOutRecordMapper.selectItemLinesPage(params);
        int total = stockOutRecordMapper.selectItemLinesCount();
        return PagePayload.of(rows, total, page, pageSize);
    }

    @Override
    @Transactional
    public StockOutRecord create(StockOutRecord stockOutRecord) {
        if (stockOutRecord.getProductId() == null || stockOutRecord.getWarehouseId() == null) {
            throw new IllegalArgumentException("商品与仓库不能为空");
        }
        if (stockOutRecord.getQuantity() == null || stockOutRecord.getQuantity() <= 0) {
            throw new IllegalArgumentException("出库数量必须大于0");
        }
        // 生成批次号
        String batchNumber = "OUT" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        stockOutRecord.setBatchNumber(batchNumber);
        
        // 计算总价
        Double unitPrice = stockOutRecord.getUnitPrice();
        if (unitPrice == null || unitPrice <= 0) {
            throw new IllegalArgumentException("出库单价必须大于0");
        }
        Double totalPrice = unitPrice * stockOutRecord.getQuantity();
        stockOutRecord.setTotalPrice(totalPrice);
        
        // 设置默认值
        stockOutRecord.setStatus(1);
        stockOutRecord.setCreateTime(new Date());
        stockOutRecord.setUpdateTime(new Date());
        stockOutRecord.setOperatorId(SecurityUtils.currentUserIdOrNull());

        // 插入出库记录
        stockOutRecordMapper.insert(stockOutRecord);
        
        // 出库操作应该减少仓库库存
        int inventoryChange = -stockOutRecord.getQuantity();
        // 更新仓库库存
        inventoryService.updateQuantity(stockOutRecord.getProductId(), stockOutRecord.getWarehouseId(), inventoryChange);
        
        // 如果是超市补货，还需要增加商品库存
        if ("超市补货".equals(stockOutRecord.getOutType())) {
            // 增加商品库存
            Product product = productMapper.selectById(stockOutRecord.getProductId());
            if (product != null) {
                product.setQuantity(product.getQuantity() + stockOutRecord.getQuantity());
                productMapper.update(product);
            }
        }
        
        return stockOutRecord;
    }

    @Override
    public StockOutRecord update(StockOutRecord stockOutRecord) {
        stockOutRecord.setUpdateTime(new Date());
        stockOutRecordMapper.update(stockOutRecord);
        return stockOutRecord;
    }

    @Override
    public boolean delete(Long id) {
        return stockOutRecordMapper.delete(id) > 0;
    }

    @Override
    public List<StockOutRecord> getByProductId(Long productId) {
        return stockOutRecordMapper.selectByProductId(productId);
    }
}
