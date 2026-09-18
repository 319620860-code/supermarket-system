package com.supermarket.backend.controller;

import com.supermarket.backend.entity.StockInRecord;
import com.supermarket.backend.service.AuditLogService;
import com.supermarket.backend.service.StockInRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-in")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
public class StockInRecordController {

    @Autowired
    private StockInRecordService stockInRecordService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 根据入库记录ID获取入库记录
     * @param id 入库记录ID
     * @return 入库记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockInRecord> getStockInRecordById(@PathVariable Long id) {
        StockInRecord record = stockInRecordService.getById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * 根据批次号获取入库记录
     * @param batchNumber 批次号
     * @return 入库记录
     */
    @GetMapping("/batch/{batchNumber}")
    public ResponseEntity<StockInRecord> getStockInRecordByBatchNumber(@PathVariable String batchNumber) {
        StockInRecord record = stockInRecordService.getByBatchNumber(batchNumber);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * 分页获取入库记录列表
     * @param keyword 搜索关键词
     * @param productId 商品ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 入库记录列表和总数
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStockInRecordPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> pageData = stockInRecordService.getPage(keyword, productId, status, startDate, endDate, page, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 分页获取入库记录列表（简化版，供前端调用）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 入库记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getStockInRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        int p = page != null ? page : (pageNum != null ? pageNum : 1);
        Map<String, Object> pageData = stockInRecordService.getItemLinePage(p, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 创建入库记录
     * @param stockInRecord 入库记录
     * @return 创建后的入库记录
     */
    @PostMapping
    public ResponseEntity<StockInRecord> createStockInRecord(@RequestBody StockInRecord stockInRecord) {
        StockInRecord createdRecord = stockInRecordService.create(stockInRecord);
        auditLogService.log(
                "STOCK_IN_CREATE",
                "id=" + createdRecord.getId() + ",recordNo=" + createdRecord.getBatchNumber());
        return ResponseEntity.ok(createdRecord);
    }

    /**
     * 更新入库记录
     * @param id 入库记录ID
     * @param stockInRecord 入库记录
     * @return 更新后的入库记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockInRecord> updateStockInRecord(@PathVariable Long id, @RequestBody StockInRecord stockInRecord) {
        stockInRecord.setId(id);
        StockInRecord updatedRecord = stockInRecordService.update(stockInRecord);
        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * 删除入库记录
     * @param id 入库记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteStockInRecord(@PathVariable Long id) {
        boolean deleted = stockInRecordService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    /**
     * 获取指定商品的入库记录
     * @param productId 商品ID
     * @return 入库记录列表
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockInRecord>> getStockInRecordsByProductId(@PathVariable Long productId) {
        List<StockInRecord> records = stockInRecordService.getByProductId(productId);
        return ResponseEntity.ok(records);
    }
}