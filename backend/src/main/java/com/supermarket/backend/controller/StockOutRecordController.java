package com.supermarket.backend.controller;

import com.supermarket.backend.entity.StockOutRecord;
import com.supermarket.backend.service.AuditLogService;
import com.supermarket.backend.service.StockOutRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-out")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
public class StockOutRecordController {

    @Autowired
    private StockOutRecordService stockOutRecordService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 根据出库记录ID获取出库记录
     * @param id 出库记录ID
     * @return 出库记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockOutRecord> getStockOutRecordById(@PathVariable Long id) {
        StockOutRecord record = stockOutRecordService.getById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * 根据批次号获取出库记录
     * @param batchNumber 批次号
     * @return 出库记录
     */
    @GetMapping("/batch/{batchNumber}")
    public ResponseEntity<StockOutRecord> getStockOutRecordByBatchNumber(@PathVariable String batchNumber) {
        StockOutRecord record = stockOutRecordService.getByBatchNumber(batchNumber);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * 分页获取出库记录列表
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
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStockOutRecordPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String outType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> pageData = stockOutRecordService.getPage(keyword, productId, outType, status, startDate, endDate, page, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 分页获取出库记录列表（简化版，供前端调用）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 出库记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getStockOutRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        int p = page != null ? page : (pageNum != null ? pageNum : 1);
        Map<String, Object> pageData = stockOutRecordService.getItemLinePage(p, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 创建出库记录
     * @param stockOutRecord 出库记录
     * @return 创建后的出库记录
     */
    @PostMapping
    public ResponseEntity<StockOutRecord> createStockOutRecord(@RequestBody StockOutRecord stockOutRecord) {
        StockOutRecord createdRecord = stockOutRecordService.create(stockOutRecord);
        auditLogService.log(
                "STOCK_OUT_CREATE",
                "id=" + createdRecord.getId() + ",recordNo=" + createdRecord.getBatchNumber());
        return ResponseEntity.ok(createdRecord);
    }

    /**
     * 更新出库记录
     * @param id 出库记录ID
     * @param stockOutRecord 出库记录
     * @return 更新后的出库记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockOutRecord> updateStockOutRecord(@PathVariable Long id, @RequestBody StockOutRecord stockOutRecord) {
        stockOutRecord.setId(id);
        StockOutRecord updatedRecord = stockOutRecordService.update(stockOutRecord);
        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * 删除出库记录
     * @param id 出库记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteStockOutRecord(@PathVariable Long id) {
        boolean deleted = stockOutRecordService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    /**
     * 获取指定商品的出库记录
     * @param productId 商品ID
     * @return 出库记录列表
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockOutRecord>> getStockOutRecordsByProductId(@PathVariable Long productId) {
        List<StockOutRecord> records = stockOutRecordService.getByProductId(productId);
        return ResponseEntity.ok(records);
    }
}

