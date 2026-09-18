package com.supermarket.backend.service;

import com.supermarket.backend.entity.SysUser;
import com.supermarket.backend.entity.SysRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SysUserService {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);
    
    /**
     * 根据用户ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser getById(Long id);
    
    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建后的用户信息
     */
    SysUser create(SysUser user);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    SysUser update(SysUser user);
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean delete(Long id);
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param role 选择的角色
     * @return 用户信息
     * @throws IllegalArgumentException 登录失败时抛出异常
     */
    SysUser login(String username, String password, String role) throws IllegalArgumentException;
    
    /**
     * 用户登录（兼容旧接口）
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     * @throws IllegalArgumentException 登录失败时抛出异常
     */
    default SysUser login(String username, String password) throws IllegalArgumentException {
        return login(username, password, null);
    }

    /**
     * 设置用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否设置成功
     */
    boolean setRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getUserRoles(Long userId);

    /**
     * 获取所有用户
     * @return 用户列表
     */
    List<SysUser> listAllUsers();

    /**
     * 分页获取用户列表
     * @param pageable 分页参数
     * @param keyword 搜索关键字
     * @return 用户分页列表
     */
    Page<SysUser> listUsers(Pageable pageable, String keyword, Integer status, Integer gender);
}
