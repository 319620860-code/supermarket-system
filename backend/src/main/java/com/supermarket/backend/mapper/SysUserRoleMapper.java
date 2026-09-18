package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {

    /**
     * 根据用户ID查询用户角色关联
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<SysUserRole> selectByUserId(Long userId);

    /**
     * 根据角色ID查询用户角色关联
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    List<SysUserRole> selectByRoleId(Long roleId);

    /**
     * 批量插入用户角色关联
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 插入条数
     */
    int batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 删除用户角色关联
     * @param userId 用户ID
     * @return 删除条数
     */
    int deleteByUserId(Long userId);

    /**
     * 删除用户角色关联
     * @param roleId 角色ID
     * @return 删除条数
     */
    int deleteByRoleId(Long roleId);
}