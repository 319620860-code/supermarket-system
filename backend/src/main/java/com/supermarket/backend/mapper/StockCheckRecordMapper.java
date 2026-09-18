package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.StockCheckRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockCheckRecordMapper {
    /**
     * 根据ID查询盘点记录
     * @param id 盘点记录ID
     * @return 盘点记录
     */
    Map<String, Object> selectById(Long id);

    /**
     * 根据盘点单号查询盘点记录
     * @param checkNumber 盘点单号
     * @return 盘点记录
     */
    StockCheckRecord selectByCheckNumber(String checkNumber);

    /**
     * 分页查询盘点记录
     * @param params 查询参数
     * @return 盘点记录列表
     */
    List<StockCheckRecord> selectPage(Map<String, Object> params);

    /**
     * 查询盘点记录总数
     * @param params 查询参数
     * @return 记录总数
     */
    int selectCount(Map<String, Object> params);

    /**
     * 插入盘点记录
     * @param stockCheckRecord 盘点记录
     * @return 插入结果
     */
    int insert(StockCheckRecord stockCheckRecord);

    /**
     * 更新盘点记录
     * @param stockCheckRecord 盘点记录
     * @return 更新结果
     */
    int update(StockCheckRecord stockCheckRecord);

    /**
     * 删除盘点记录
     * @param id 盘点记录ID
     * @return 删除结果
     */
    int delete(Long id);

    /**
     * 查询指定商品的盘点记录
     * @param productId 商品ID
     * @return 盘点记录列表
     */
    List<StockCheckRecord> selectByProductId(Long productId);

    /**
     * 分页获取盘点记录列表（包含商品和操作人员信息）
     * @param params 查询参数
     * @return 盘点记录列表
     */
    List<Map<String, Object>> selectItemLinesPage(Map<String, Object> params);

    /**
     * 查询盘点记录总数
     * @return 记录总数
     */
    int selectItemLinesCount();
}
