package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.CategoryMapper;
import com.supermarket.backend.mapper.ProductMapper;
import com.supermarket.backend.entity.Category;
import com.supermarket.backend.entity.Product;
import com.supermarket.backend.service.ProductService;
import com.supermarket.backend.util.PagePayload;
import com.supermarket.backend.util.SpecificationUnitNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Product getById(Long id) {
        Product p = productMapper.selectById(id);
        fillCategoryNameIfMissing(p);
        return p;
    }

    @Override
    public Product getByBarcode(String barcode, boolean skuOnly, boolean sellableOnly) {
        if (!StringUtils.hasText(barcode)) {
            throw new IllegalArgumentException("商品条码不能为空");
        }
        if (skuOnly) {
            return null;
        }
        Product p = productMapper.selectByBarcode(barcode.trim());
        fillCategoryNameIfMissing(p);
        if (sellableOnly && !isSellable(p)) {
            return null;
        }
        return p;
    }

    /** 上架且所属分类启用，才允许收银售出 */
    private boolean isSellable(Product p) {
        if (p == null) {
            return false;
        }
        if (p.getStatus() == null || p.getStatus() != 1) {
            return false;
        }
        if (p.getCategoryId() == null) {
            return false;
        }
        Category c = categoryMapper.selectById(p.getCategoryId());
        return c != null && c.getStatus() != null && c.getStatus() == 1;
    }

    private void fillCategoryNameIfMissing(Product p) {
        if (p == null || StringUtils.hasText(p.getCategoryName()) || p.getCategoryId() == null) {
            return;
        }
        Category c = categoryMapper.selectById(p.getCategoryId());
        if (c != null) {
            p.setCategoryName(c.getName());
        }
    }

    @Override
    public Map<String, Object> getPage(String keyword, Long categoryId, Integer status, Boolean forSale, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        int offset = (page - 1) * pageSize;
        List<Product> products = productMapper.selectPage(keyword, categoryId, status, forSale, offset, pageSize);
        for (Product p : products) {
            fillCategoryNameIfMissing(p);
        }
        int total = productMapper.selectCount(keyword, categoryId, status, forSale);

        return PagePayload.of(products, total, page, pageSize);
    }

    @Override
    public Product create(Product product) {
        if (product == null || !StringUtils.hasText(product.getName())) {
            throw new IllegalArgumentException("商品信息不完整");
        }

        // 检查条码是否已存在
        if (!StringUtils.hasText(product.getBarcode())) {
            product.setBarcode("BC" + System.currentTimeMillis());
        }
        if (StringUtils.hasText(product.getBarcode())) {
            Product existingProduct = productMapper.selectByBarcode(product.getBarcode());
            if (existingProduct != null) {
                throw new IllegalArgumentException("商品条码已存在");
            }
        }

        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setStatus(1); // 默认启用
        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }
        if (product.getMinStock() == null) {
            product.setMinStock(10); // 默认最低库存为10
        }
        if (product.getPurchasePrice() == null) {
            product.setPurchasePrice(java.math.BigDecimal.ZERO);
        }
        if (product.getSellingPrice() == null) {
            product.setSellingPrice(java.math.BigDecimal.ZERO);
        }
        if (product.getSpecification() != null) {
            product.setSpecification(SpecificationUnitNormalizer.normalize(product.getSpecification()));
        }

        productMapper.insert(product);
        return productMapper.selectById(product.getId());
    }

    @Override
    public Product update(Product product) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("商品信息不完整");
        }

        Product existingProduct = productMapper.selectById(product.getId());
        if (existingProduct == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        if (StringUtils.hasText(product.getBarcode())) {
            Product barcodeProduct = productMapper.selectByBarcode(product.getBarcode());
            if (barcodeProduct != null && !barcodeProduct.getId().equals(product.getId())) {
                throw new IllegalArgumentException("商品条码已存在");
            }
        }

        if (product.getSpecification() != null) {
            product.setSpecification(SpecificationUnitNormalizer.normalize(product.getSpecification()));
        }
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);
        return productMapper.selectById(product.getId());
    }

    @Override
    public boolean updateStock(Long id, Integer quantity) {
        if (id == null || quantity == null) {
            throw new IllegalArgumentException("参数不完整");
        }

        Product existingProduct = productMapper.selectById(id);
        if (existingProduct == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        return productMapper.updateStock(id, quantity) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        return productMapper.delete(id) > 0;
    }

    @Override
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("商品ID列表不能为空");
        }

        int count = 0;
        for (Long id : ids) {
            if (productMapper.delete(id) > 0) {
                count++;
            }
        }

        return count;
    }
}
