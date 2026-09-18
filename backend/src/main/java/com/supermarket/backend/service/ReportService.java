package com.supermarket.backend.service;

import java.util.List;
import java.util.Map;

public interface ReportService {
    /**
     * 获取销售统计报表
     * @param params 查询参数
     * @return 销售统计数据
     */
    List<Map<String, Object>> getSalesStatistics(Map<String, Object> params);

    /**
     * 获取商品销售排行榜
     * @param params 查询参数
     * @return 商品销售排行
     */
    List<Map<String, Object>> getProductSalesRanking(Map<String, Object> params);

    /**
     * 获取库存报表
     * @param params 查询参数
     * @return 库存数据
     */
    List<Map<String, Object>> getInventoryReport(Map<String, Object> params);

    /**
     * 获取低库存商品报表
     * @param params 查询参数
     * @return 低库存商品列表
     */
    List<Map<String, Object>> getLowInventoryReport(Map<String, Object> params);

    /**
     * 获取供应商报表
     * @param params 查询参数
     * @return 供应商数据
     */
    List<Map<String, Object>> getSupplierReport(Map<String, Object> params);

    /**
     * 获取销售趋势报表
     * @param params 查询参数
     * @return 销售趋势数据
     */
    List<Map<String, Object>> getSalesTrend(Map<String, Object> params);

    /**
     * 获取销售利润报表
     * @param params 查询参数
     * @return 销售利润数据
     */
    List<Map<String, Object>> getSalesProfit(Map<String, Object> params);
}
