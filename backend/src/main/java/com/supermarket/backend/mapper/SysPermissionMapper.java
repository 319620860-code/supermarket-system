package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper {

    /**
     * 根据ID查询权限
     * @param id 权限ID
     * @return 权限信息
     */
    SysPermission selectById(Long id);

    /**
     * 根据代码查询权限
     * @param code 权限代码
     * @return 权限信息
     */
    SysPermission selectByCode(String code);

    /**
     * 查询所有权限
     * @return 权限列表
     */
    List<SysPermission> selectAll();

    /**
     * 查询所有菜单权限
     * @return 菜单权限列表
     */
    List<SysPermission> selectAllMenus();

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> selectByUserId(Long userId);

    /**
     * 插入权限
     * @param permission 权限信息
     * @return 插入条数
     */
    int insert(SysPermission permission);

    /**
     * 更新权限
     * @param permission 权限信息
     * @return 更新条数
     */
    int update(SysPermission permission);

    /**
     * 删除权限
     * @param id 权限ID
     * @return 删除条数
     */
    int delete(Long id);

    /**
     * 根据父ID查询子权限
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<SysPermission> selectByParentId(Long parentId);
}