package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    SysRole selectById(Long id);

    /**
     * 根据代码查询角色
     * @param code 角色代码
     * @return 角色信息
     */
    SysRole selectByCode(String code);

    /**
     * 查询所有角色
     * @return 角色列表
     */
    List<SysRole> selectAll();

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectByUserId(Long userId);

    /**
     * 插入角色
     * @param role 角色信息
     * @return 插入条数
     */
    int insert(SysRole role);

    /**
     * 更新角色
     * @param role 角色信息
     * @return 更新条数
     */
    int update(SysRole role);

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除条数
     */
    int delete(Long id);

    /**
     * 批量插入角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 插入条数
     */
    int batchInsertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除角色权限
     * @param roleId 角色ID
     * @return 删除条数
     */
    int deleteRolePermissions(Long roleId);
}