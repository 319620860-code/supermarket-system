package com.supermarket.backend.service;

import com.supermarket.backend.entity.SysRole;
import com.supermarket.backend.entity.SysPermission;

import java.util.List;

public interface SysRoleService {

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    SysRole getById(Long id);

    /**
     * 根据代码查询角色
     * @param code 角色代码
     * @return 角色信息
     */
    SysRole getByCode(String code);

    /**
     * 查询所有角色
     * @return 角色列表
     */
    List<SysRole> getAll();

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getByUserId(Long userId);

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建后的角色信息
     */
    SysRole create(SysRole role);

    /**
     * 更新角色信息
     * @param role 角色信息
     * @return 更新后的角色信息
     */
    SysRole update(SysRole role);

    /**
     * 删除角色
     * @param id 角色ID
     * @return 是否删除成功
     */
    boolean delete(Long id);

    /**
     * 设置角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否设置成功
     */
    boolean setPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> getRolePermissions(Long roleId);
}