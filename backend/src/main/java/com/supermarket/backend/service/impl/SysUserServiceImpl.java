package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.SysUserMapper;
import com.supermarket.backend.mapper.SysUserRoleMapper;
import com.supermarket.backend.mapper.SysRoleMapper;
import com.supermarket.backend.entity.SysUser;
import com.supermarket.backend.entity.SysRole;
import com.supermarket.backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public SysUser create(SysUser user) {
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(1); // 默认启用
        sysUserMapper.insert(user);
        return user;
    }

    @Override
    public SysUser update(SysUser user) {
        // 如果密码被修改，需要重新加密
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.update(user);
        return user;
    }

    @Override
    public boolean delete(Long id) {
        return sysUserMapper.delete(id) > 0;
    }

    @Override
    public SysUser login(String username, String password, String role) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 暂时允许明文密码登录以便调试
        if (!passwordEncoder.matches(password, user.getPassword()) && !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("请选择登录身份");
        }

        String roleTrim = role.trim();
        String uname = username.trim();

        // 内置业务账号：登录身份必须与账号一致（避免 admin 账号在任意身份下都能登录）
        if (uname.equalsIgnoreCase("admin") && !"admin".equalsIgnoreCase(roleTrim)) {
            throw new IllegalArgumentException("管理员账号请使用「管理员」身份登录");
        }
        if (uname.equalsIgnoreCase("cashier") && !"cashier".equalsIgnoreCase(roleTrim)) {
            throw new IllegalArgumentException("收银员账号请使用「收银员」身份登录");
        }
        if (uname.equalsIgnoreCase("stock") && !"stock".equalsIgnoreCase(roleTrim)) {
            throw new IllegalArgumentException("库存管理员账号请使用「库存管理员」身份登录");
        }

        List<SysRole> userRoles = getUserRoles(user.getId());
        boolean hasRole = userRoles.stream()
                .anyMatch(r -> roleTrim.equalsIgnoreCase(r.getCode()));
        if (!hasRole) {
            throw new IllegalArgumentException("您没有权限以所选身份登录，请确认账号已分配该角色");
        }

        return user;
    }

    @Override
    @Transactional
    public boolean setRoles(Long userId, List<Long> roleIds) {
        // 先删除原有的角色
        sysUserRoleMapper.deleteByUserId(userId);
        // 如果角色列表不为空，添加新的角色
        if (roleIds != null && !roleIds.isEmpty()) {
            return sysUserRoleMapper.batchInsert(userId, roleIds) > 0;
        }
        return true;
    }

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        return sysRoleMapper.selectByUserId(userId);
    }

    @Override
    public List<SysUser> listAllUsers() {
        return sysUserMapper.selectAll();
    }

    @Override
    public Page<SysUser> listUsers(Pageable pageable, String keyword, Integer status, Integer gender) {
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();
        
        List<SysUser> users = sysUserMapper.selectUsersByPage(keyword, status, gender, offset, limit);
        int total = sysUserMapper.countUsersByPage(keyword, status, gender);
        
        return new PageImpl<>(users, pageable, total);
    }
}
