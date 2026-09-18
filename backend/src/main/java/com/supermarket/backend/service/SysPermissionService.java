package com.supermarket.backend.service;

import com.supermarket.backend.entity.SysPermission;

import java.util.List;

public interface SysPermissionService {

    /**
     * 根据ID查询权限
     * @param id 权限ID
     * @return 权限信息
     */
    SysPermission getById(Long id);

    /**
     * 根据代码查询权限
     * @param code 权限代码
     * @return 权限信息
     */
    SysPermission getByCode(String code);

    /**
     * 查询所有权限
     * @return 权限列表
     */
    List<SysPermission> getAll();

    /**
     * 查询所有菜单权限
     * @return 菜单权限列表
     */
    List<SysPermission> getAllMenus();

    /**
     * 根据用户ID查询权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> getByUserId(Long userId);

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> getByRoleId(Long roleId);

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建后的权限信息
     */
    SysPermission create(SysPermission permission);

    /**
     * 更新权限信息
     * @param permission 权限信息
     * @return 更新后的权限信息
     */
    SysPermission update(SysPermission permission);

    /**
     * 删除权限
     * @param id 权限ID
     * @return 是否删除成功
     */
    boolean delete(Long id);

    /**
     * 获取权限树结构
     * @return 权限树列表
     */
    List<SysPermission> getPermissionTree();

    /**
     * 根据用户ID获取权限树
     * @param userId 用户ID
     * @return 权限树列表
     */
    List<SysPermission> getPermissionTreeByUserId(Long userId);
}