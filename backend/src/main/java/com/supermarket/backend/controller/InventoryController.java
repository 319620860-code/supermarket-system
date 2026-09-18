package com.supermarket.backend.controller;

import com.supermarket.backend.entity.Inventory;
import com.supermarket.backend.service.InventoryService;
import com.supermarket.backend.util.PagePayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/inventories", "/api/inventory"})
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * 根据库存ID获取库存信息
     * @param id 库存ID
     * @return 库存信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        try {
            Inventory inventory = inventoryService.getById(id);
            if (inventory == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            // 数据库表不存在或其他异常，返回404
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 分页获取库存列表
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param status 库存状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 库存列表和总数
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventoryPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String stockLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = inventoryService.getPage(keyword, productId, warehouseId, status, stockLevel, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 数据库表不存在或其他异常，返回空列表
            return ResponseEntity.ok(PagePayload.of(Collections.emptyList(), 0, page, pageSize));
        }
    }

    /**
     * 根据商品ID和仓库ID获取库存信息
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @return 库存信息
     */
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<Inventory> getInventoryByProductIdAndWarehouseId(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        try {
            Inventory inventory = inventoryService.getByProductIdAndWarehouseId(productId, warehouseId);
            if (inventory == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            // 数据库表不存在或其他异常，返回404
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建库存
     * @param inventory 库存信息
     * @return 创建后的库存信息
     */
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        try {
            Inventory createdInventory = inventoryService.create(inventory);
            return ResponseEntity.ok(createdInventory);
        } catch (Exception e) {
            // 数据库表不存在或其他异常，返回500错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新库存信息
     * @param id 库存ID
     * @param inventory 库存信息
     * @return 更新后的库存信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        try {
            inventory.setId(id);
            Inventory updatedInventory = inventoryService.update(inventory);
            return ResponseEntity.ok(updatedInventory);
        } catch (Exception e) {
            // 数据库表不存在或其他异常，返回500错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新库存数量
     * @param id 库存ID
     * @param quantity 库存数量
     * @return 成功响应
     */
    @PutMapping("/{id}/quantity")
    public ResponseEntity<Void> updateInventoryQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            inventoryService.updateQuantity(id, quantity);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 数据库表不存在或其他异常，返回500错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除库存
     * @param id 库存ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 数据库表不存在或其他异常，返回500错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}