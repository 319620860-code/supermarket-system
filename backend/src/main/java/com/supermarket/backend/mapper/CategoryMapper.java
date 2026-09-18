package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    /**
     * 根据分类ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    Category selectById(@Param("id") Long id);
    
    /**
     * 查询所有分类
     * @return 分类列表
     */
    List<Category> selectAll();
    
    /**
     * 根据父分类ID查询子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 插入分类
     * @param category 分类信息
     * @return 影响行数
     */
    int insert(Category category);
    
    /**
     * 更新分类信息
     * @param category 分类信息
     * @return 影响行数
     */
    int update(Category category);
    
    /**
     * 删除分类
     * @param id 分类ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 检查分类下是否有商品
     * @param id 分类ID
     * @return 商品数量
     */
    int countProductsByCategoryId(@Param("id") Long id);
}
