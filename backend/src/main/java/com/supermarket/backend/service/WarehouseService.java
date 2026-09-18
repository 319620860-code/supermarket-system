package com.supermarket.backend.service;

import com.supermarket.backend.entity.Warehouse;

import java.util.List;
import java.util.Map;

public interface WarehouseService {
    /**
     * 根据仓库ID查询仓库
     * @param id 仓库ID
     * @return 仓库信息
     */
    Warehouse getById(Long id);

    /**
     * 分页查询仓库列表
     * @param keyword 搜索关键词
     * @param status 仓库状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 仓库列表和总数
     */
    Map<String, Object> getPage(String keyword, String name, String address, Integer status, Integer page, Integer pageSize);

    /**
     * 创建仓库
     * @param warehouse 仓库信息
     * @return 创建后的仓库信息
     */
    Warehouse create(Warehouse warehouse);

    /**
     * 更新仓库信息
     * @param warehouse 仓库信息
     * @return 更新后的仓库信息
     */
    Warehouse update(Warehouse warehouse);

    /**
     * 删除仓库
     * @param id 仓库ID
     */
    void delete(Long id);

    /**
     * 获取所有仓库列表
     * @return 仓库列表
     */
    List<Warehouse> getAll();
}