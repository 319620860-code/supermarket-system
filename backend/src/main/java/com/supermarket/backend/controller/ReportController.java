package com.supermarket.backend.controller;

import com.supermarket.backend.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    
    @Autowired
    private ReportService reportService;

    /**
     * 获取销售统计报表
     * @param params 查询参数
     * @return 销售统计报表数据
     */
    @PostMapping("/sales-statistics")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER')")
    public ResponseEntity<Map<String, Object>> getSalesStatistics(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> statisticsList = reportService.getSalesStatistics(params);
        Map<String, Object> result = new HashMap<>();
        
        // 如果有数据，取第一条作为总统计数据
        if (!statisticsList.isEmpty()) {
            Map<String, Object> statistics = statisticsList.get(0);
            result.putAll(statistics);
        }
        
        // 添加趋势数据
        Map<String, Object> trendParams = new HashMap<>(params);
        trendParams.put("type", "daily");
        List<Map<String, Object>> trendData = reportService.getSalesTrend(trendParams);
        result.put("trendData", trendData);
        
        // 添加默认值，确保前端有数据显示
        result.putIfAbsent("totalSales", 0.0);
        result.putIfAbsent("totalOrders", 0);
        result.putIfAbsent("averageOrderValue", 0.0);
        result.putIfAbsent("salesGrowth", 0.0);
        result.putIfAbsent("orderGrowth", 0.0);
        result.putIfAbsent("avgGrowth", 0.0);
        result.putIfAbsent("salesGrowthRate", 0.0);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取商品销售排行榜
     * @param params 查询参数
     * @return 商品销售排行榜数据
     */
    @PostMapping("/sales-ranking")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER')")
    public ResponseEntity<Map<String, Object>> getProductSalesRanking(@RequestBody Map<String, Object> params) {
        try {
            List<Map<String, Object>> rankList = reportService.getProductSalesRanking(params);
            if (rankList == null) {
                rankList = Collections.emptyList();
            }

            // 计算总销售额（JDBC 可能返回 BigDecimal，避免强转 Double 导致 ClassCastException）
            double totalSales = 0.0;
            for (Map<String, Object> product : rankList) {
                Object amt = product.get("salesAmount");
                if (amt instanceof Number) {
                    totalSales += ((Number) amt).doubleValue();
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("rankList", rankList);
            response.put("totalSales", totalSales);
            response.put("salesRatio", 100.0); // 这里可以根据需要计算占比
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取商品销售排行失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 获取库存报表
     * @param params 查询参数
     * @return 库存报表数据
     */
    @PostMapping("/inventory")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<Map<String, Object>> getInventoryReport(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> inventoryList = reportService.getInventoryReport(params);
        Map<String, Object> result = new HashMap<>();
        result.put("inventoryList", inventoryList);
        
        // 计算库存统计数据
        double totalInventoryValue = 0.0;
        int totalInventoryCount = 0;
        int lowInventoryCount = 0;
        
        for (Map<String, Object> item : inventoryList) {
            Object valueObj = item.get("inventory_value");
            if (valueObj == null) {
                valueObj = item.get("inventoryValue");
            }
            if (valueObj instanceof Number) {
                totalInventoryValue += ((Number) valueObj).doubleValue();
            }

            totalInventoryCount++;

            Object qtyObj = item.get("quantity");
            Object minObj = item.get("min_stock");
            if (minObj == null) {
                minObj = item.get("minStock");
            }
            if (qtyObj instanceof Number && minObj instanceof Number) {
                if (((Number) qtyObj).doubleValue() <= ((Number) minObj).doubleValue()) {
                    lowInventoryCount++;
                }
            }
        }
        
        result.put("totalInventoryValue", totalInventoryValue);
        result.put("totalInventoryCount", totalInventoryCount);
        result.put("lowInventoryCount", lowInventoryCount);
        // 周转率需按成本与销量单独核算，此处不返回虚构数值
        
        return ResponseEntity.ok(result);
    }



    /**
     * 获取低库存商品报表
     * @param params 查询参数
     * @return 低库存商品报表数据
     */
    @PostMapping("/low-inventory")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<List<Map<String, Object>>> getLowInventoryReport(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> result = reportService.getLowInventoryReport(params);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取供应商报表
     * @param params 查询参数
     * @return 供应商报表数据
     */
    @PostMapping("/supplier")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
    public ResponseEntity<List<Map<String, Object>>> getSupplierReport(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> result = reportService.getSupplierReport(params);
        return ResponseEntity.ok(result);
    }



    /**
     * 获取销售利润报表
     * @param params 查询参数
     * @return 销售利润报表数据
     */
    @PostMapping("/profit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER')")
    public ResponseEntity<List<Map<String, Object>>> getSalesProfit(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> result = reportService.getSalesProfit(params);
        return ResponseEntity.ok(result);
    }
}