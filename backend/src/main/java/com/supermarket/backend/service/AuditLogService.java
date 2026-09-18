package com.supermarket.backend.service;

import com.supermarket.backend.mapper.SysAuditLogMapper;
import com.supermarket.backend.entity.SysAuditLog;
import com.supermarket.backend.util.SecurityUtils;
import com.supermarket.backend.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关键操作审计（失败不影响主业务）
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private SysAuditLogMapper sysAuditLogMapper;

    public void log(String action, String detail) {
        try {
            String username = SecurityUtils.currentUsernameOrNull();
            String ip = WebUtils.clientIp();
            String d = detail;
            if (d != null && d.length() > 500) {
                d = d.substring(0, 500);
            }
            SysAuditLog row = new SysAuditLog();
            row.setUsername(username != null ? username : "unknown");
            row.setAction(action);
            row.setDetail(d);
            row.setIp(ip != null && !ip.isEmpty() ? ip : null);
            sysAuditLogMapper.insert(row);
        } catch (Exception e) {
            log.warn("写入操作审计失败: {}", e.getMessage());
        }
    }

    /**
     * @param action        精确匹配操作码，空表示不限
     * @param usernameKeyword 用户名模糊匹配，空表示不限
     */
    public Map<String, Object> page(int page, int pageSize, String action, String usernameKeyword) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        String actionTrim = action != null ? action.trim() : "";
        if (actionTrim.isEmpty()) {
            actionTrim = null;
        }
        String userLike = usernameKeyword != null ? usernameKeyword.trim() : "";
        if (userLike.isEmpty()) {
            userLike = null;
        }
        int offset = (page - 1) * pageSize;
        long total = sysAuditLogMapper.countByCondition(actionTrim, userLike);
        List<SysAuditLog> records = sysAuditLogMapper.selectPageByCondition(actionTrim, userLike, offset, pageSize);
        Map<String, Object> out = new HashMap<>();
        out.put("records", records);
        out.put("total", total);
        out.put("page", page);
        out.put("pageSize", pageSize);
        return out;
    }
}
