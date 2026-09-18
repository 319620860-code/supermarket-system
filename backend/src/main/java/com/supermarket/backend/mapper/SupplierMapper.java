package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SupplierMapper {
    /**
     * 根据ID查询供应商
     * @param id 供应商ID
     * @return 供应商信息
     */
    Supplier selectById(Long id);

    /**
     * 分页查询供应商
     * @param params 查询参数
     * @return 供应商列表
     */
    List<Supplier> selectPage(Map<String, Object> params);

    /**
     * 查询供应商总数
     * @param params 查询参数
     * @return 供应商总数
     */
    int selectCount(Map<String, Object> params);

    /**
     * 插入供应商
     * @param supplier 供应商信息
     * @return 插入结果
     */
    int insert(Supplier supplier);

    /**
     * 更新供应商
     * @param supplier 供应商信息
     * @return 更新结果
     */
    int update(Supplier supplier);

    /**
     * 删除供应商
     * @param id 供应商ID
     * @return 删除结果
     */
    int delete(Long id);

    /**
     * 获取所有供应商
     * @return 供应商列表
     */
    List<Supplier> selectAll();
}
