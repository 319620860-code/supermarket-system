package com.supermarket.backend.mapper;

import com.supermarket.backend.dto.StockOutItemLineVO;
import com.supermarket.backend.entity.StockOutRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockOutRecordMapper {
    /**
     * 根据ID查询出库记录
     * @param id 出库记录ID
     * @return 出库记录
     */
    StockOutRecord selectById(Long id);

    /**
     * 根据批次号查询出库记录
     * @param batchNumber 批次号
     * @return 出库记录
     */
    StockOutRecord selectByBatchNumber(String batchNumber);

    /**
     * 分页查询出库记录
     * @param params 查询参数
     * @return 出库记录列表
     */
    List<StockOutRecord> selectPage(Map<String, Object> params);

    /**
     * 查询出库记录总数
     * @param params 查询参数
     * @return 记录总数
     */
    int selectCount(Map<String, Object> params);

    /**
     * 出库明细分页（联查主单、订单号）
     */
    List<StockOutItemLineVO> selectItemLinesPage(Map<String, Object> params);

    int selectItemLinesCount();

    /**
     * 插入出库记录
     * @param stockOutRecord 出库记录
     * @return 插入结果
     */
    int insert(StockOutRecord stockOutRecord);

    /**
     * 更新出库记录
     * @param stockOutRecord 出库记录
     * @return 更新结果
     */
    int update(StockOutRecord stockOutRecord);

    /**
     * 删除出库记录
     * @param id 出库记录ID
     * @return 删除结果
     */
    int delete(Long id);

    /**
     * 查询指定商品的出库记录
     * @param productId 商品ID
     * @return 出库记录列表
     */
    List<StockOutRecord> selectByProductId(Long productId);
}
