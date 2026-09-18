package com.supermarket.backend.util;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.supermarket.backend.entity.Product;
import com.supermarket.backend.entity.ProductExcelData;
import com.supermarket.backend.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductExcelListener implements ReadListener<ProductExcelData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductExcelListener.class);
    private static final int BATCH_COUNT = 100;
    
    private final ProductService productService;
    private final List<Product> cachedProducts = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    public ProductExcelListener(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void invoke(ProductExcelData data, AnalysisContext context) {
        LOGGER.info("解析到一条商品数据: {}", data);
        
        Product product = new Product();
        product.setBarcode(data.getBarcode());
        product.setName(data.getName());
        product.setCategoryId(data.getCategoryId());
        product.setSpecification(data.getSpecification());
        product.setUnit(data.getUnit());
        product.setPurchasePrice(data.getPurchasePrice() != null ? BigDecimal.valueOf(data.getPurchasePrice()) : null);
        product.setSellingPrice(data.getSellingPrice() != null ? BigDecimal.valueOf(data.getSellingPrice()) : null);
        product.setQuantity(data.getQuantity() != null ? data.getQuantity() : 0);
        product.setMinStock(data.getAlertThreshold() != null ? data.getAlertThreshold() : 10);
        product.setStatus(data.getStatus() != null ? data.getStatus() : 1);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        cachedProducts.add(product);

        if (cachedProducts.size() >= BATCH_COUNT) {
            saveData();
            cachedProducts.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        LOGGER.info("所有数据解析完成！");
    }

    private void saveData() {
        LOGGER.info("开始批量保存{}条商品数据", cachedProducts.size());
        for (Product product : cachedProducts) {
            try {
                productService.create(product);
            } catch (Exception e) {
                LOGGER.error("保存商品失败: {}", product.getName(), e);
            }
        }
        LOGGER.info("批量保存成功");
    }
}
