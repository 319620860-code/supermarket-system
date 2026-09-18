package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.CategoryMapper;
import com.supermarket.backend.entity.Category;
import com.supermarket.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<Category> getAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public List<Category> getByParentId(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectAll();
        Map<Long, List<Category>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        List<Category> rootCategories = new ArrayList<>(categoryMap.getOrDefault(0L, new ArrayList<>()));
        buildCategoryTree(rootCategories, categoryMap);

        return rootCategories;
    }

    @Override
    public Category create(Category category) {
        if (category == null || !StringUtils.hasText(category.getName())) {
            throw new IllegalArgumentException("分类信息不完整");
        }

        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setStatus(1); // 默认启用

        // 设置分类层级
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parentCategory = categoryMapper.selectById(category.getParentId());
            if (parentCategory == null) {
                throw new IllegalArgumentException("父分类不存在");
            }
            category.setLevel(parentCategory.getLevel() + 1);
        }

        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Category category) {
        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException("分类信息不完整");
        }

        Category existingCategory = categoryMapper.selectById(category.getId());
        if (existingCategory == null) {
            throw new IllegalArgumentException("分类不存在");
        }

        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
        return category;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        // 检查是否有子分类
        List<Category> subCategories = categoryMapper.selectByParentId(id);
        if (!subCategories.isEmpty()) {
            throw new IllegalArgumentException("该分类下有子分类，无法删除");
        }

        // 检查是否有商品引用
        int productCount = categoryMapper.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalArgumentException("该分类下还有商品，请先修改商品类别");
        }

        return categoryMapper.delete(id) > 0;
    }

    /**
     * 构建分类树
     * @param categories 分类列表
     * @param categoryMap 分类映射
     */
    private void buildCategoryTree(List<Category> categories, Map<Long, List<Category>> categoryMap) {
        for (Category category : categories) {
            List<Category> children = categoryMap.getOrDefault(category.getId(), new ArrayList<>());
            if (!children.isEmpty()) {
                category.setChildren(children);
                buildCategoryTree(children, categoryMap);
            }
        }
    }
}
