package com.supermarket.backend.controller;

import com.supermarket.backend.entity.SysUser;
import com.supermarket.backend.entity.SysRole;
import com.supermarket.backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer gender) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SysUser> userPage = sysUserService.listUsers(pageable, keyword, status, gender);
        
        long totalElements = userPage.getTotalElements();
        int pages = size > 0 ? (int) ((totalElements + size - 1) / size) : 0;
        Map<String, Object> data = new HashMap<>();
        data.put("records", userPage.getContent());
        data.put("total", totalElements);
        data.put("current", page);
        data.put("pageSize", size);
        data.put("pages", pages);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", user);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建成功的用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody SysUser user) {
        SysUser newUser = sysUserService.create(user);
        Map<String, Object> response = new HashMap<>();
        response.put("data", newUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param user 更新后的用户信息
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        SysUser updatedUser = sysUserService.update(user);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        boolean success = sysUserService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", success);
        return ResponseEntity.ok(response);
    }

    /**
     * 设置用户角色
     * @param userId 用户ID
     * @param requestBody 包含角色ID列表的请求体
     * @return 设置结果
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> setUserRoles(@PathVariable Long userId, @RequestBody Map<String, List<Long>> requestBody) {
        List<Long> roleIds = requestBody.get("roleIds");
        boolean success = sysUserService.setRoles(userId, roleIds);
        Map<String, Object> response = new HashMap<>();
        response.put("data", success);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户角色
     * @param userId 用户ID
     * @return 角色列表
     */
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserRoles(@PathVariable Long userId) {
        List<SysRole> roles = sysUserService.getUserRoles(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", roles);
        return ResponseEntity.ok(response);
    }
}
