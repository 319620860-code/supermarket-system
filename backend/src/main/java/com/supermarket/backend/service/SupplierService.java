package com.supermarket.backend.service;

import com.supermarket.backend.entity.Supplier;

import java.util.List;
import java.util.Map;

public interface SupplierService {
    /**
     * 根据ID获取供应商
     * @param id 供应商ID
     * @return 供应商信息
     */
    Supplier getById(Long id);

    /**
     * 分页查询供应商
     * @param keyword 搜索关键词
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 供应商列表和总数
     */
    Map<String, Object> getPage(String keyword, String name, String contactPerson, Integer status, Integer page, Integer pageSize);

    /**
     * 创建供应商
     * @param supplier 供应商信息
     * @return 创建后的供应商信息
     */
    Supplier create(Supplier supplier);

    /**
     * 更新供应商信息
     * @param supplier 供应商信息
     * @return 更新后的供应商信息
     */
    Supplier update(Supplier supplier);

    /**
     * 删除供应商
     * @param id 供应商ID
     * @return 删除结果
     */
    boolean delete(Long id);

    /**
     * 批量删除供应商
     * @param ids 供应商ID列表
     * @return 成功删除的数量
     */
    int batchDelete(List<Long> ids);

    /**
     * 获取所有供应商
     * @return 供应商列表
     */
    List<Supplier> getAll();
}
