package com.supermarket.backend.security;

import com.supermarket.backend.entity.SysUser;
import com.supermarket.backend.entity.SysRole;
import com.supermarket.backend.service.SysUserService;
import com.supermarket.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserService sysUserService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 记录请求信息
            logger.debug("请求URL: {}", request.getRequestURL().toString());
            logger.debug("请求方法: {}", request.getMethod());
            logger.debug("Authorization头: {}", request.getHeader("Authorization"));

            // 从请求头中获取JWT令牌
            String token = getTokenFromRequest(request);
            logger.debug("从请求头中提取的token: {}", token);

            // 验证令牌
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                logger.debug("token有效");
                // 从令牌中获取用户名
                String username = jwtUtil.getUsernameFromToken(token);
                logger.debug("从token中获取的用户名: {}", username);

                // 根据用户名获取用户信息
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    logger.debug("获取到用户信息: {}", user.getUsername());
                    // 获取用户的实际角色
                    List<SysRole> roles = sysUserService.getUserRoles(user.getId());
                    logger.debug("用户角色列表: {}", roles);
                    String activeRole = jwtUtil.getActiveRoleFromToken(token);
                    // 将角色转换为GrantedAuthority对象列表
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    if (StringUtils.hasText(activeRole)) {
                        boolean matched = roles != null && roles.stream()
                                .anyMatch(r -> r.getCode() != null && activeRole.equalsIgnoreCase(r.getCode()));
                        if (matched) {
                            String code = roles.stream()
                                    .filter(r -> r.getCode() != null && activeRole.equalsIgnoreCase(r.getCode()))
                                    .map(SysRole::getCode)
                                    .findFirst()
                                    .orElse(activeRole);
                            String authority = code.startsWith("ROLE_") ? code : "ROLE_" + code.toUpperCase();
                            authorities.add(new SimpleGrantedAuthority(authority));
                        }
                        // 若 token 伪造 activeRole 与账号角色不符，不授予权限（后续接口将 403）
                    } else if (roles != null && !roles.isEmpty()) {
                        // 旧 token 无 activeRole：保留原行为，授予账号全部角色
                        for (SysRole role : roles) {
                            String code = role.getCode();
                            if (code != null && !code.isEmpty()) {
                                String authority = code.startsWith("ROLE_") ? code : "ROLE_" + code.toUpperCase();
                                authorities.add(new SimpleGrantedAuthority(authority));
                            }
                        }
                    } else {
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }
                    logger.debug("设置的权限: {}", authorities);

                    // 设置认证信息到Spring Security上下文
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 将用户信息设置到请求属性中，方便后续Controller使用
                    request.setAttribute("currentUser", user);
                    logger.debug("认证成功，已设置认证信息到Spring Security上下文");
                } else {
                    logger.debug("未找到用户信息: {}", username);
                }
            } else {
                logger.debug("token无效或为空");
            }
        } catch (Exception ex) {
            logger.error("认证失败: {}", ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取JWT令牌
     * @param request 请求
     * @return JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}