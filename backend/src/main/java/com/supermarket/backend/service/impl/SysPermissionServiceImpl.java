package com.supermarket.backend.service.impl;

import com.supermarket.backend.mapper.SysPermissionMapper;
import com.supermarket.backend.entity.SysPermission;
import com.supermarket.backend.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl implements SysPermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public SysPermission getById(Long id) {
        return sysPermissionMapper.selectById(id);
    }

    @Override
    public SysPermission getByCode(String code) {
        return sysPermissionMapper.selectByCode(code);
    }

    @Override
    public List<SysPermission> getAll() {
        return sysPermissionMapper.selectAll();
    }

    @Override
    public List<SysPermission> getAllMenus() {
        return sysPermissionMapper.selectAllMenus();
    }

    @Override
    public List<SysPermission> getByUserId(Long userId) {
        return sysPermissionMapper.selectByUserId(userId);
    }

    @Override
    public List<SysPermission> getByRoleId(Long roleId) {
        return sysPermissionMapper.selectByRoleId(roleId);
    }

    @Override
    @Transactional
    public SysPermission create(SysPermission permission) {
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        sysPermissionMapper.insert(permission);
        return permission;
    }

    @Override
    @Transactional
    public SysPermission update(SysPermission permission) {
        permission.setUpdateTime(LocalDateTime.now());
        sysPermissionMapper.update(permission);
        return permission;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return sysPermissionMapper.delete(id) > 0;
    }

    @Override
    public List<SysPermission> getPermissionTree() {
        List<SysPermission> allPermissions = sysPermissionMapper.selectAllMenus();
        return buildPermissionTree(allPermissions);
    }

    @Override
    public List<SysPermission> getPermissionTreeByUserId(Long userId) {
        List<SysPermission> userPermissions = sysPermissionMapper.selectByUserId(userId);
        // 只保留菜单类型的权限
        List<SysPermission> menuPermissions = userPermissions.stream()
                .filter(p -> p.getType() == 1)
                .collect(Collectors.toList());
        return buildPermissionTree(menuPermissions);
    }

    /**
     * 构建权限树
     * @param permissions 权限列表
     * @return 权限树列表
     */
    private List<SysPermission> buildPermissionTree(List<SysPermission> permissions) {
        // 将权限列表转换为Map，便于查找
        Map<Long, SysPermission> permissionMap = permissions.stream()
                .collect(Collectors.toMap(SysPermission::getId, p -> p));

        List<SysPermission> rootPermissions = new ArrayList<>();

        // 构建权限树
        permissions.forEach(permission -> {
            Long parentId = permission.getParentId();
            if (parentId == null || parentId == 0) {
                // 根权限
                rootPermissions.add(permission);
            } else {
                // 非根权限，添加到父权限的children列表中
                SysPermission parentPermission = permissionMap.get(parentId);
                if (parentPermission != null) {
                    List<SysPermission> children = parentPermission.getChildren();
                    if (children == null) {
                        children = new ArrayList<>();
                        parentPermission.setChildren(children);
                    }
                    children.add(permission);
                }
            }
        });

        return rootPermissions;
    }
}