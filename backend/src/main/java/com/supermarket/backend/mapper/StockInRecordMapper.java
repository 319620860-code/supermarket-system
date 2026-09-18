package com.supermarket.backend.mapper;

import com.supermarket.backend.dto.StockInItemLineVO;
import com.supermarket.backend.entity.StockInRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockInRecordMapper {
    /**
     * 根据ID查询入库记录
     * @param id 入库记录ID
     * @return 入库记录
     */
    StockInRecord selectById(Long id);

    /**
     * 根据批次号查询入库记录
     * @param batchNumber 批次号
     * @return 入库记录
     */
    StockInRecord selectByBatchNumber(String batchNumber);

    /**
     * 分页查询入库记录
     * @param params 查询参数
     * @return 入库记录列表
     */
    List<StockInRecord> selectPage(Map<String, Object> params);

    /**
     * 查询入库记录总数
     * @param params 查询参数
     * @return 记录总数
     */
    int selectCount(Map<String, Object> params);

    /**
     * 入库明细分页（联查主单、供应商）
     */
    List<StockInItemLineVO> selectItemLinesPage(Map<String, Object> params);

    int selectItemLinesCount();

    /**
     * 插入入库记录
     * @param stockInRecord 入库记录
     * @return 插入结果
     */
    int insert(StockInRecord stockInRecord);

    /**
     * 更新入库记录
     * @param stockInRecord 入库记录
     * @return 更新结果
     */
    int update(StockInRecord stockInRecord);

    /**
     * 删除入库记录
     * @param id 入库记录ID
     * @return 删除结果
     */
    int delete(Long id);

    /**
     * 查询指定商品的入库记录
     * @param productId 商品ID
     * @return 入库记录列表
     */
    List<StockInRecord> selectByProductId(Long productId);

    /**
     * 查询供应商报表
     * @param params 查询参数
     * @return 供应商报表数据
     */
    List<Map<String, Object>> selectSupplierReport(Map<String, Object> params);
}
