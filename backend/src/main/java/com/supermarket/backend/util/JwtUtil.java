package com.supermarket.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成JWT令牌
     * @param username 用户名
     * @param roles 用户角色列表
     * @return JWT令牌
     */
    public String generateToken(String username, List<String> roles) {
        return generateToken(username, roles, null);
    }

    /**
     * 生成JWT令牌（含本次登录所选身份，用于 Spring Security 仅授予该身份权限）
     * @param activeRole 登录时选择的角色码，如 admin、cashier、stock；可为 null（兼容旧调用）
     */
    public String generateToken(String username, List<String> roles, String activeRole) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("created", now);
        claims.put("roles", roles != null ? roles : new ArrayList<>());
        if (StringUtils.hasText(activeRole)) {
            claims.put("activeRole", activeRole.trim());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成JWT令牌（重载方法，兼容原有调用）
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        return generateToken(username, new ArrayList<>());
    }

    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("无效的JWT令牌: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT令牌参数错误: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从JWT令牌中获取声明
     * @param token JWT令牌
     * @return 声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从JWT令牌中获取角色列表
     * @param token JWT令牌
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        return new ArrayList<>();
    }

    /**
     * 本次登录所选身份（与登录页一致）
     */
    public String getActiveRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object ar = claims.get("activeRole");
        return ar != null ? ar.toString().trim() : null;
    }

    /**
     * 获取签名密钥
     * @return 签名密钥
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
