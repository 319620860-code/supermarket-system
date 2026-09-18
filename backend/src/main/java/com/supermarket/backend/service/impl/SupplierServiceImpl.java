package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.SupplierMapper;
import com.supermarket.backend.entity.Supplier;
import com.supermarket.backend.service.SupplierService;
import com.supermarket.backend.util.PagePayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public Supplier getById(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getPage(String keyword, String name, String contactPerson, Integer status, Integer page, Integer pageSize) {
        // 设置默认值，确保page和pageSize不为null
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("name", name);
        params.put("contactPerson", contactPerson);
        params.put("status", status);
        params.put("offset", (page - 1) * pageSize);
        params.put("pageSize", pageSize);

        List<Supplier> suppliers = supplierMapper.selectPage(params);
        int total = supplierMapper.selectCount(params);

        return PagePayload.of(suppliers, total, page, pageSize);
    }

    @Override
    public Supplier create(Supplier supplier) {
        supplier.setCreateTime(new Date());
        supplier.setUpdateTime(new Date());
        supplierMapper.insert(supplier);
        return supplier;
    }

    @Override
    public Supplier update(Supplier supplier) {
        supplier.setUpdateTime(new Date());
        supplierMapper.update(supplier);
        return supplier;
    }

    @Override
    public boolean delete(Long id) {
        return supplierMapper.delete(id) > 0;
    }

    @Override
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("供应商ID列表不能为空");
        }

        int count = 0;
        for (Long id : ids) {
            if (supplierMapper.delete(id) > 0) {
                count++;
            }
        }

        return count;
    }

    @Override
    public List<Supplier> getAll() {
        return supplierMapper.selectAll();
    }
}
