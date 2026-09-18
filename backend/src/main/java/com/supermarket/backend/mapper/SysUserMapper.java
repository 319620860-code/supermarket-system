package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysUserMapper {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser selectById(@Param("id") Long id);
    
    /**
     * 插入用户
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(SysUser user);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 影响行数
     */
    int update(SysUser user);
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<SysUser> selectAll();

    /**
     * 分页查询用户列表
     * @param keyword 搜索关键字
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户列表
     */
    List<SysUser> selectUsersByPage(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("gender") Integer gender,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 分页查询用户总数
     */
    int countUsersByPage(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("gender") Integer gender);
}
