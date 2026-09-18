package com.supermarket.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.supermarket.backend.entity.Product;
import com.supermarket.backend.entity.ProductExcelData;
import com.supermarket.backend.service.ProductService;
import com.supermarket.backend.util.ProductExcelListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商品详情（主档，供详情页一次拉取）
     */
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> body = new HashMap<>(4);
        body.put("product", product);
        return ResponseEntity.ok(body);
    }

    /**
     * 根据商品ID获取商品信息
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 根据条码查询商品：先匹配 SKU 条码；skuOnly=true 时仅收银场景（不回落主档条码）。
     * sellableOnly=true 时仅返回可售商品（上架且分类启用），否则 404。
     */
    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<Product> getProductByBarcode(
            @PathVariable String barcode,
            @RequestParam(value = "skuOnly", defaultValue = "false") boolean skuOnly,
            @RequestParam(value = "sellableOnly", defaultValue = "false") boolean sellableOnly) {
        Product product = productService.getByBarcode(barcode, skuOnly, sellableOnly);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 分页获取商品列表
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 商品状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 商品列表和总数
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<Map<String, Object>> getProductPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean forSale,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> pageData = productService.getPage(keyword, categoryId, status, forSale, page, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 创建商品
     * @param product 商品信息
     * @return 创建后的商品信息
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.create(product);
        return ResponseEntity.ok(createdProduct);
    }

    /**
     * 更新商品信息
     * @param id 商品ID
     * @param product 商品信息
     * @return 更新后的商品信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        Product updatedProduct = productService.update(product);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 更新商品库存
     * @param id 商品ID
     * @param quantity 库存数量
     * @return 更新结果
     */
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Boolean> updateProductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean updated = productService.updateStock(id, quantity);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除商品
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    /**
     * 批量删除商品
     * @param ids 商品ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Integer> batchDeleteProducts(@RequestBody List<Long> ids) {
        int deletedCount = productService.batchDelete(ids);
        return ResponseEntity.ok(deletedCount);
    }

    /**
     * 批量导入商品
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Map<String, Object>> importProducts(@RequestParam("file") MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), ProductExcelData.class, new ProductExcelListener(productService)).sheet().doRead();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "导入成功");
        return ResponseEntity.ok(result);
    }
}
