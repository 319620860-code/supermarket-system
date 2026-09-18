package com.supermarket.backend.controller;

import com.supermarket.backend.entity.Warehouse;
import com.supermarket.backend.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * List all warehouses. Must be registered before {@code /{id}} so {@code /all} is not matched as an id.
     */
    @GetMapping("/all")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.getAll();
        return ResponseEntity.ok(warehouses);
    }

    /**
     * 根据仓库ID获取仓库信息
     * @param id 仓库ID
     * @return 仓库信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getById(id);
        if (warehouse == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(warehouse);
    }

    /**
     * 分页获取仓库列表
     * @param keyword 搜索关键词
     * @param status 仓库状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 仓库列表和总数
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWarehousePage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = warehouseService.getPage(keyword, name, address, status, page, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 创建仓库
     * @param warehouse 仓库信息
     * @return 创建后的仓库信息
     */
    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        Warehouse createdWarehouse = warehouseService.create(warehouse);
        return ResponseEntity.ok(createdWarehouse);
    }

    /**
     * 更新仓库信息
     * @param id 仓库ID
     * @param warehouse 仓库信息
     * @return 更新后的仓库信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        Warehouse updatedWarehouse = warehouseService.update(warehouse);
        return ResponseEntity.ok(updatedWarehouse);
    }

    /**
     * 删除仓库
     * @param id 仓库ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}