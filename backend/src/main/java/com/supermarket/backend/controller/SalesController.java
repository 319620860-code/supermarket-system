package com.supermarket.backend.controller;

import com.supermarket.backend.service.ReportService;
import com.supermarket.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER')")
public class SalesController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderService orderService;

    /**
     * 获取销售报表
     * @param params 查询参数
     * @return 销售报表数据
     */
    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> getSalesReport(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> statisticsList = reportService.getSalesStatistics(params);
        Map<String, Object> result = new HashMap<>();
        
        // 如果有数据，取第一条作为总统计数据
        if (!statisticsList.isEmpty()) {
            Map<String, Object> statistics = statisticsList.get(0);
            result.putAll(statistics);
        }
        
        // 添加趋势数据
        String startDate = (String) params.get("startDate");
        String endDate = (String) params.get("endDate");
        Map<String, Object> trendParams = new HashMap<>();
        trendParams.put("startDate", startDate);
        trendParams.put("endDate", endDate);
        List<Map<String, Object>> trendData = reportService.getSalesTrend(trendParams);
        result.put("trendData", trendData);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取销售图表数据
     * @param type 图表类型（daily/monthly/product）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售图表数据
     */
    @GetMapping("/chart")
    public ResponseEntity<Map<String, Object>> getSalesChart(
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        
        List<Map<String, Object>> chartData;
        if ("product".equals(type)) {
            chartData = reportService.getProductSalesRanking(params);
        } else {
            chartData = reportService.getSalesTrend(params);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", chartData);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取销售统计数据
     * @return 销售统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSalesStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        String todayStart = today + " 00:00:00";
        String todayEnd = today + " 23:59:59";
        LocalDate monthStart = today.withDayOfMonth(1);
        String monthStartStr = monthStart + " 00:00:00";

        double todaySales = orderService.getTodaySales() != null ? orderService.getTodaySales() : 0.0;
        stats.put("todaySales", todaySales);

        double monthSales = orderService.getSalesByDateRange(monthStartStr, todayEnd);
        stats.put("monthSales", monthSales);

        int todayOrders = orderService.getOrderCountByDateRange(todayStart, todayEnd);
        int monthOrders = orderService.getOrderCountByDateRange(monthStartStr, todayEnd);
        stats.put("todayOrders", todayOrders);
        stats.put("monthOrders", monthOrders);

        // 环比需额外统计昨日/上月，此处占位为 0，避免虚构比例
        stats.put("todayChange", 0.0);
        stats.put("monthChange", 0.0);
        stats.put("todayOrdersChange", 0.0);
        stats.put("monthOrdersChange", 0.0);

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取销售排行
     * @param params 查询参数
     * @return 销售排行数据
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> getSalesRanking(@RequestParam Map<String, Object> params) {
        List<Map<String, Object>> result = reportService.getProductSalesRanking(params);
        return ResponseEntity.ok(result);
    }
}