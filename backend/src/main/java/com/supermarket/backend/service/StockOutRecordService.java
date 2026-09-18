package com.supermarket.backend.service;

import com.supermarket.backend.entity.StockOutRecord;

import java.util.List;
import java.util.Map;

public interface StockOutRecordService {
    /**
     * 根据ID获取出库记录
     * @param id 出库记录ID
     * @return 出库记录
     */
    StockOutRecord getById(Long id);

    /**
     * 根据批次号获取出库记录
     * @param batchNumber 批次号
     * @return 出库记录
     */
    StockOutRecord getByBatchNumber(String batchNumber);

    /**
     * 分页查询出库记录
     * @param keyword 搜索关键词
     * @param productId 商品ID
     * @param outType 出库类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 出库记录列表和总数
     */
    Map<String, Object> getPage(String keyword, Long productId, String outType, Integer status, String startDate, String endDate, Integer page, Integer pageSize);

    /**
     * 按出库明细分页（联查商品、订单号），供库存页「出库记录」列表
     */
    Map<String, Object> getItemLinePage(Integer page, Integer pageSize);

    /**
     * 创建出库记录并更新库存
     * @param stockOutRecord 出库记录
     * @return 创建后的出库记录
     */
    StockOutRecord create(StockOutRecord stockOutRecord);

    /**
     * 更新出库记录
     * @param stockOutRecord 出库记录
     * @return 更新后的出库记录
     */
    StockOutRecord update(StockOutRecord stockOutRecord);

    /**
     * 删除出库记录
     * @param id 出库记录ID
     * @return 删除结果
     */
    boolean delete(Long id);

    /**
     * 获取指定商品的出库记录
     * @param productId 商品ID
     * @return 出库记录列表
     */
    List<StockOutRecord> getByProductId(Long productId);
}
