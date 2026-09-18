package com.supermarket.backend.service;

import com.supermarket.backend.entity.Category;

import java.util.List;

public interface CategoryService {
    
    /**
     * 根据分类ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    Category getById(Long id);
    
    /**
     * 查询所有分类
     * @return 分类列表
     */
    List<Category> getAll();
    
    /**
     * 根据父分类ID查询子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getByParentId(Long parentId);
    
    /**
     * 获取分类树
     * @return 分类树
     */
    List<Category> getCategoryTree();
    
    /**
     * 创建分类
     * @param category 分类信息
     * @return 创建后的分类信息
     */
    Category create(Category category);
    
    /**
     * 更新分类信息
     * @param category 分类信息
     * @return 更新后的分类信息
     */
    Category update(Category category);
    
    /**
     * 删除分类
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean delete(Long id);
}
