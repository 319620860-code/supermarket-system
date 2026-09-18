package com.supermarket.backend.service;

import com.supermarket.backend.entity.StockCheckRecord;

import java.util.List;
import java.util.Map;

public interface StockCheckRecordService {
    /**
     * 根据ID获取盘点记录
     * @param id 盘点记录ID
     * @return 盘点记录
     */
    Map<String, Object> getById(Long id);

    /**
     * 根据盘点单号获取盘点记录
     * @param checkNumber 盘点单号
     * @return 盘点记录
     */
    StockCheckRecord getByCheckNumber(String checkNumber);

    /**
     * 分页查询盘点记录
     * @param keyword 搜索关键词
     * @param productId 商品ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 盘点记录列表和总数
     */
    Map<String, Object> getPage(String keyword, Long productId, Integer status, String startDate, String endDate, Integer page, Integer pageSize);

    /**
     * 创建盘点记录
     * @param stockCheckRecord 盘点记录
     * @return 创建后的盘点记录
     */
    StockCheckRecord create(StockCheckRecord stockCheckRecord);

    /**
     * 更新盘点记录
     * @param stockCheckRecord 盘点记录
     * @return 更新后的盘点记录
     */
    StockCheckRecord update(StockCheckRecord stockCheckRecord);

    /**
     * 删除盘点记录
     * @param id 盘点记录ID
     * @return 删除结果
     */
    boolean delete(Long id);

    /**
     * 获取指定商品的盘点记录
     * @param productId 商品ID
     * @return 盘点记录列表
     */
    List<StockCheckRecord> getByProductId(Long productId);

    /**
     * 确认盘点结果并更新库存
     * @param id 盘点记录ID
     * @return 更新后的盘点记录
     */
    Map<String, Object> confirmCheck(Long id);

    /**
     * 分页获取盘点记录列表（简化版，供前端调用）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 盘点记录列表
     */
    Map<String, Object> getItemLinePage(Integer page, Integer pageSize);
}
