package com.supermarket.backend.service;

import com.supermarket.backend.entity.StockInRecord;

import java.util.List;
import java.util.Map;

public interface StockInRecordService {
    /**
     * 根据ID获取入库记录
     * @param id 入库记录ID
     * @return 入库记录
     */
    StockInRecord getById(Long id);

    /**
     * 根据批次号获取入库记录
     * @param batchNumber 批次号
     * @return 入库记录
     */
    StockInRecord getByBatchNumber(String batchNumber);

    /**
     * 分页查询入库记录
     * @param keyword 搜索关键词
     * @param productId 商品ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 入库记录列表和总数
     */
    Map<String, Object> getPage(String keyword, Long productId, Integer status, String startDate, String endDate, Integer page, Integer pageSize);

    /**
     * 按入库明细分页（联查商品、供应商），供库存页「入库记录」列表
     */
    Map<String, Object> getItemLinePage(Integer page, Integer pageSize);

    /**
     * 创建入库记录并更新库存
     * @param stockInRecord 入库记录
     * @return 创建后的入库记录
     */
    StockInRecord create(StockInRecord stockInRecord);

    /**
     * 更新入库记录
     * @param stockInRecord 入库记录
     * @return 更新后的入库记录
     */
    StockInRecord update(StockInRecord stockInRecord);

    /**
     * 删除入库记录
     * @param id 入库记录ID
     * @return 删除结果
     */
    boolean delete(Long id);

    /**
     * 获取指定商品的入库记录
     * @param productId 商品ID
     * @return 入库记录列表
     */
    List<StockInRecord> getByProductId(Long productId);
}
