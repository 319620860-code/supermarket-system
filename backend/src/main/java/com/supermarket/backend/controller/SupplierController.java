package com.supermarket.backend.controller;

import com.supermarket.backend.entity.Supplier;
import com.supermarket.backend.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 根据供应商ID获取供应商信息
     * @param id 供应商ID
     * @return 供应商信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        if (supplier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(supplier);
    }

    /**
     * 分页获取供应商列表
     * @param keyword 搜索关键词
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 供应商列表和总数
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSupplierPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> pageData = supplierService.getPage(keyword, name, contactPerson, status, page, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 创建供应商
     * @param supplier 供应商信息
     * @return 创建后的供应商信息
     */
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        Supplier createdSupplier = supplierService.create(supplier);
        return ResponseEntity.ok(createdSupplier);
    }

    /**
     * 更新供应商信息
     * @param id 供应商ID
     * @param supplier 供应商信息
     * @return 更新后的供应商信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        Supplier updatedSupplier = supplierService.update(supplier);
        return ResponseEntity.ok(updatedSupplier);
    }

    /**
     * 删除供应商
     * @param id 供应商ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteSupplier(@PathVariable Long id) {
        boolean deleted = supplierService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Integer> batchDeleteSuppliers(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(supplierService.batchDelete(ids));
    }

    /**
     * 获取所有供应商
     * @return 供应商列表
     */
    @GetMapping("/all")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAll();
        return ResponseEntity.ok(suppliers);
    }
}
