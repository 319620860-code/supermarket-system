package com.supermarket.backend.controller;

import com.supermarket.backend.dto.StockCheckRequest;
import com.supermarket.backend.entity.StockCheckRecord;
import com.supermarket.backend.service.StockCheckRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-check")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STOCK')")
public class StockCheckRecordController {

    @Autowired
    private StockCheckRecordService stockCheckRecordService;

    /**
     * 根据盘点记录ID获取盘点记录
     * @param id 盘点记录ID
     * @return 盘点记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStockCheckRecordById(@PathVariable Long id) {
        Map<String, Object> record = stockCheckRecordService.getById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * 根据盘点单号获取盘点记录
     * @param checkNumber 盘点单号
     * @return 盘点记录
     */
    @GetMapping("/check-number/{checkNumber}")
    public ResponseEntity<StockCheckRecord> getStockCheckRecordByCheckNumber(@PathVariable String checkNumber) {
        StockCheckRecord record = stockCheckRecordService.getByCheckNumber(checkNumber);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * 分页获取盘点记录列表
     * @param keyword 搜索关键词
     * @param productId 商品ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 盘点记录列表和总数
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStockCheckRecordPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> pageData = stockCheckRecordService.getPage(keyword, productId, status, startDate, endDate, page, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 创建盘点记录
     * @param stockCheckRecord 盘点记录
     * @return 创建后的盘点记录
     */
    @PostMapping
    public ResponseEntity<?> createStockCheckRecord(@RequestBody StockCheckRequest request) {
        // 如果是批量创建
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (StockCheckRecord record : request.getItems()) {
                // 设置备注为描述
                if (request.getDescription() != null) {
                    record.setRemark(request.getDescription());
                }
                stockCheckRecordService.create(record);
            }
            return ResponseEntity.ok("批量创建盘点记录成功");
        } else {
            // 单个创建的情况（为了向后兼容）
            StockCheckRecord record = new StockCheckRecord();
            record.setRemark(request.getDescription());
            StockCheckRecord createdRecord = stockCheckRecordService.create(record);
            return ResponseEntity.ok(createdRecord);
        }
    }

    /**
     * 更新盘点记录
     * @param id 盘点记录ID
     * @param stockCheckRecord 盘点记录
     * @return 更新后的盘点记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockCheckRecord> updateStockCheckRecord(@PathVariable Long id, @RequestBody StockCheckRecord stockCheckRecord) {
        stockCheckRecord.setId(id);
        StockCheckRecord updatedRecord = stockCheckRecordService.update(stockCheckRecord);
        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * 删除盘点记录
     * @param id 盘点记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteStockCheckRecord(@PathVariable Long id) {
        boolean deleted = stockCheckRecordService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    /**
     * 获取指定商品的盘点记录
     * @param productId 商品ID
     * @return 盘点记录列表
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockCheckRecord>> getStockCheckRecordsByProductId(@PathVariable Long productId) {
        List<StockCheckRecord> records = stockCheckRecordService.getByProductId(productId);
        return ResponseEntity.ok(records);
    }

    /**
     * 确认盘点结果
     * @param id 盘点记录ID
     * @return 确认后的盘点记录
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmStockCheckRecord(@PathVariable Long id) {
        Map<String, Object> confirmedRecord = stockCheckRecordService.confirmCheck(id);
        return ResponseEntity.ok(confirmedRecord);
    }

    /**
     * 分页获取盘点记录列表（简化版，供前端调用）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 盘点记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getStockCheckRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        int p = page != null ? page : (pageNum != null ? pageNum : 1);
        Map<String, Object> pageData = stockCheckRecordService.getItemLinePage(p, pageSize);
        return ResponseEntity.ok(pageData);
    }
}
