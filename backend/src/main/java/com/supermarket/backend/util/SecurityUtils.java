package com.supermarket.backend.util;

import com.supermarket.backend.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户（JWT 认证后 Principal 为 {@link SysUser}）
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsernameOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object p = auth.getPrincipal();
        if (p instanceof SysUser u) {
            return u.getUsername();
        }
        return null;
    }

    /** 当前登录用户主键（JWT Principal 为 {@link SysUser}） */
    public static Long currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object p = auth.getPrincipal();
        if (p instanceof SysUser u) {
            return u.getId();
        }
        return null;
    }

    /** 展示用：优先真实姓名，否则用户名 */
    public static String currentUserDisplayNameOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object p = auth.getPrincipal();
        if (p instanceof SysUser u) {
            if (u.getRealName() != null && !u.getRealName().isBlank()) {
                return u.getRealName();
            }
            return u.getUsername();
        }
        return null;
    }
}
