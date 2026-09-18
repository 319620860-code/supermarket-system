package com.supermarket.backend.controller;

import com.supermarket.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 操作审计查询（仅管理员）
 */
@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username) {
        return ResponseEntity.ok(auditLogService.page(page, pageSize, action, username));
    }
}
