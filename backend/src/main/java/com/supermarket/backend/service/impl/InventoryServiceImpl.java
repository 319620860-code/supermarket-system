package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.CategoryMapper;
import com.supermarket.backend.mapper.InventoryMapper;
import com.supermarket.backend.mapper.ProductMapper;
import com.supermarket.backend.mapper.WarehouseMapper;
import com.supermarket.backend.entity.Category;
import com.supermarket.backend.entity.Inventory;
import com.supermarket.backend.entity.Product;
import com.supermarket.backend.entity.Warehouse;
import com.supermarket.backend.service.InventoryService;
import com.supermarket.backend.util.PagePayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Inventory getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("库存ID不能为空");
        }
        return inventoryMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getPage(String keyword, Long productId, Long warehouseId, Integer status, String stockLevel, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("productId", productId);
        params.put("warehouseId", warehouseId);
        params.put("status", status);
        params.put("stockLevel", stockLevel);
        params.put("offset", (page - 1) * pageSize);
        params.put("limit", pageSize);

        List<Inventory> inventories = inventoryMapper.selectPage(params);
        int total = inventoryMapper.selectCount(params);

        return PagePayload.of(inventories, total, page, pageSize);
    }

    @Override
    public Inventory getByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        if (productId == null || warehouseId == null) {
            throw new IllegalArgumentException("商品ID和仓库ID不能为空");
        }
        return inventoryMapper.selectByProductIdAndWarehouseId(productId, warehouseId);
    }

    @Override
    public List<Inventory> listByProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        return inventoryMapper.selectByProductId(productId);
    }

    @Override
    public void syncSingleWarehouseInventoryIfPresent(Long productId, int totalQuantity) {
        if (productId == null || totalQuantity < 0) {
            return;
        }
        List<Inventory> rows = inventoryMapper.selectByProductId(productId);
        if (rows == null || rows.size() != 1) {
            return;
        }
        Inventory inv = rows.get(0);
        if (inv.getStatus() == null || inv.getStatus() != 1) {
            throw new IllegalStateException("该分仓库存记录已禁用，不能由盘点回写数量");
        }
        assertProductCategoryEnabledForStock(productId);
        assertWarehouseEnabled(inv.getWarehouseId());
        int locked = inv.getLockedQuantity() != null ? inv.getLockedQuantity() : 0;
        inv.setQuantity(totalQuantity);
        inv.setAvailableQuantity(Math.max(0, totalQuantity - locked));
        inventoryMapper.update(inv);
    }

    @Override
    public Inventory create(Inventory inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("库存信息不能为空");
        }
        if (inventory.getProductId() == null || inventory.getWarehouseId() == null) {
            throw new IllegalArgumentException("商品ID和仓库ID不能为空");
        }
        assertWarehouseEnabled(inventory.getWarehouseId());
        assertProductCategoryEnabledForStock(inventory.getProductId());
        if (inventory.getQuantity() == null || inventory.getQuantity() < 0) {
            throw new IllegalArgumentException("库存数量不能为空且必须大于等于0");
        }
        if (inventory.getLockedQuantity() == null) {
            inventory.setLockedQuantity(0);
        }
        if (inventory.getAvailableQuantity() == null) {
            inventory.setAvailableQuantity(Math.max(0, inventory.getQuantity() - inventory.getLockedQuantity()));
        }
        if (inventory.getStatus() == null) {
            inventory.setStatus(1);
        }
        if (inventory.getUnitCost() == null) {
            inventory.setUnitCost(BigDecimal.ZERO);
        }

        inventoryMapper.insert(inventory);
        return inventory;
    }

    @Override
    public Inventory update(Inventory inventory) {
        if (inventory == null || inventory.getId() == null) {
            throw new IllegalArgumentException("库存信息和ID不能为空");
        }
        if (inventory.getProductId() == null || inventory.getWarehouseId() == null) {
            throw new IllegalArgumentException("商品ID和仓库ID不能为空");
        }
        if (inventory.getQuantity() == null || inventory.getQuantity() < 0) {
            throw new IllegalArgumentException("库存数量不能为空且必须大于等于0");
        }
        assertWarehouseEnabled(inventory.getWarehouseId());
        assertProductCategoryEnabledForStock(inventory.getProductId());

        inventoryMapper.update(inventory);
        return inventory;
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        if (id == null) {
            throw new IllegalArgumentException("库存ID不能为空");
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("库存数量不能为空且必须大于等于0");
        }
        Inventory inv = inventoryMapper.selectById(id);
        if (inv == null) {
            throw new IllegalArgumentException("库存记录不存在");
        }
        if (inv.getStatus() == null || inv.getStatus() != 1) {
            throw new IllegalArgumentException("该库存记录已禁用，不能修改数量");
        }
        assertWarehouseEnabled(inv.getWarehouseId());
        assertProductCategoryEnabledForStock(inv.getProductId());
        inventoryMapper.updateQuantity(id, quantity);
    }

    @Override
    public void updateQuantity(Long productId, Long warehouseId, Integer quantity) {
        if (productId == null || warehouseId == null) {
            throw new IllegalArgumentException("商品ID和仓库ID不能为空");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("库存数量不能为空");
        }

        assertWarehouseEnabled(warehouseId);
        assertProductCategoryEnabledForStock(productId);

        // 先获取现有库存记录
        Inventory inventory = inventoryMapper.selectByProductIdAndWarehouseId(productId, warehouseId);

        if (inventory != null) {
            if (inventory.getStatus() == null || inventory.getStatus() != 1) {
                throw new IllegalArgumentException("该分仓库存记录已禁用，不能增减");
            }
            // 计算新的库存数量（支持负数，用于出库）
            int newQuantity = inventory.getQuantity() + quantity;
            if (newQuantity < 0) {
                throw new IllegalArgumentException("库存不足，无法完成操作");
            }

            // 更新库存数量和可用数量
            inventory.setQuantity(newQuantity);
            int locked = inventory.getLockedQuantity() != null ? inventory.getLockedQuantity() : 0;
            inventory.setAvailableQuantity(Math.max(0, newQuantity - locked));
            inventoryMapper.update(inventory);
        } else if (quantity > 0) {
            // 如果是入库且库存记录不存在，创建新的库存记录
            inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setWarehouseId(warehouseId);
            inventory.setQuantity(quantity);
            inventory.setLockedQuantity(0);
            inventory.setAvailableQuantity(quantity);
            inventory.setStatus(1); // 设置状态为正常
            inventoryMapper.insert(inventory);
        } else {
            // 如果是出库但库存记录不存在，抛出异常
            throw new IllegalArgumentException("库存记录不存在，无法完成出库操作");
        }
    }

    private void assertWarehouseEnabled(Long warehouseId) {
        Warehouse w = warehouseMapper.selectById(warehouseId);
        if (w == null) {
            throw new IllegalArgumentException("仓库不存在");
        }
        if (w.getStatus() == null || w.getStatus() != 1) {
            throw new IllegalArgumentException("仓库已停用，不能操作库存");
        }
    }

    private void assertProductCategoryEnabledForStock(Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (p.getStatus() == null || p.getStatus() != 1) {
            throw new IllegalArgumentException("商品已下架，不能操作库存");
        }
        if (p.getCategoryId() == null) {
            throw new IllegalArgumentException("商品未设置分类，不能操作库存");
        }
        Category c = categoryMapper.selectById(p.getCategoryId());
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            throw new IllegalArgumentException("商品所属分类已停用，不能操作库存");
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("库存ID不能为空");
        }
        inventoryMapper.delete(id);
    }

    @Override
    public void lockStock(Long productId, Long warehouseId, Integer quantity) {
        if (productId == null || warehouseId == null) {
            throw new IllegalArgumentException("商品ID和仓库ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("锁定数量必须大于0");
        }
        
        assertWarehouseEnabled(warehouseId);
        assertProductCategoryEnabledForStock(productId);
        
        int affected = inventoryMapper.lockStock(productId, warehouseId, quantity);
        if (affected == 0) {
            throw new IllegalArgumentException("库存不足或库存记录不存在，无法锁定");
        }
    }

    @Override
    public void unlockStock(Long productId, Long warehouseId, Integer quantity) {
        if (productId == null || warehouseId == null) {
            throw new IllegalArgumentException("商品ID和仓库ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("解锁数量必须大于0");
        }
        
        assertWarehouseEnabled(warehouseId);
        assertProductCategoryEnabledForStock(productId);
        
        int affected = inventoryMapper.unlockStock(productId, warehouseId, quantity);
        if (affected == 0) {
            throw new IllegalArgumentException("锁定数量不足或库存记录不存在，无法解锁");
        }
    }

    @Override
    public void deductLockedStock(Long productId, Long warehouseId, Integer quantity) {
        if (productId == null || warehouseId == null) {
            throw new IllegalArgumentException("商品ID和仓库ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("扣减数量必须大于0");
        }
        
        assertWarehouseEnabled(warehouseId);
        assertProductCategoryEnabledForStock(productId);
        
        int affected = inventoryMapper.deductLockedStock(productId, warehouseId, quantity);
        if (affected == 0) {
            throw new IllegalArgumentException("锁定数量不足或库存记录不存在，无法扣减");
        }
    }
}