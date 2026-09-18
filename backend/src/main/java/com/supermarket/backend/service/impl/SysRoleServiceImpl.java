package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.SysRoleMapper;
import com.supermarket.backend.mapper.SysPermissionMapper;
import com.supermarket.backend.entity.SysRole;
import com.supermarket.backend.entity.SysPermission;
import com.supermarket.backend.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public SysRole getById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public SysRole getByCode(String code) {
        return sysRoleMapper.selectByCode(code);
    }

    @Override
    public List<SysRole> getAll() {
        return sysRoleMapper.selectAll();
    }

    @Override
    public List<SysRole> getByUserId(Long userId) {
        return sysRoleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public SysRole create(SysRole role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public SysRole update(SysRole role) {
        role.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.update(role);
        return role;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return sysRoleMapper.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean setPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有的权限
        sysRoleMapper.deleteRolePermissions(roleId);
        // 如果权限列表不为空，添加新的权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            return sysRoleMapper.batchInsertRolePermissions(roleId, permissionIds) > 0;
        }
        return true;
    }

    @Override
    public List<SysPermission> getRolePermissions(Long roleId) {
        return sysPermissionMapper.selectByRoleId(roleId);
    }
}