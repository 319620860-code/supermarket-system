package com.supermarket.backend.controller;

import com.supermarket.backend.entity.Category;
import com.supermarket.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     * @return 分类列表
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAll();
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取分类树
     * @return 分类树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<List<Category>> getCategoryTree() {
        List<Category> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }

    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<List<Category>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getByParentId(parentId);
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据分类ID获取分类
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER','ROLE_STOCK')")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    /**
     * 创建分类
     * @param category 分类信息
     * @return 创建后的分类信息
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryService.create(category);
        return ResponseEntity.ok(createdCategory);
    }

    /**
     * 更新分类信息
     * @param id 分类ID
     * @param category 分类信息
     * @return 更新后的分类信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        Category updatedCategory = categoryService.update(category);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.delete(id);
            return ResponseEntity.ok(deleted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
