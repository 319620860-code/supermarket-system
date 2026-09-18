package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.WarehouseMapper;
import com.supermarket.backend.entity.Warehouse;
import com.supermarket.backend.service.WarehouseService;
import com.supermarket.backend.util.PagePayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public Warehouse getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        return warehouseMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getPage(String keyword, String name, String address, Integer status, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("name", name);
        params.put("address", address);
        params.put("status", status);
        params.put("offset", (page - 1) * pageSize);
        params.put("limit", pageSize);

        List<Warehouse> warehouses = warehouseMapper.selectPage(params);
        int total = warehouseMapper.selectCount(params);

        return PagePayload.of(warehouses, total, page, pageSize);
    }

    @Override
    public Warehouse create(Warehouse warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库信息不能为空");
        }
        if (!StringUtils.hasText(warehouse.getName())) {
            throw new IllegalArgumentException("仓库名称不能为空");
        }

        warehouseMapper.insert(warehouse);
        return warehouse;
    }

    @Override
    public Warehouse update(Warehouse warehouse) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new IllegalArgumentException("仓库信息和ID不能为空");
        }
        if (!StringUtils.hasText(warehouse.getName())) {
            throw new IllegalArgumentException("仓库名称不能为空");
        }

        warehouseMapper.update(warehouse);
        return warehouse;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        warehouseMapper.delete(id);
    }

    @Override
    public List<Warehouse> getAll() {
        return warehouseMapper.selectAll();
    }
}