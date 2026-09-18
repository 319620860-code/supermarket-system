package com.supermarket.backend.controller;

import com.supermarket.backend.entity.SysPermission;
import com.supermarket.backend.entity.SysUser;
import com.supermarket.backend.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class SysPermissionController {

    @Autowired
    private SysPermissionService sysPermissionService;

    /**
     * 获取所有权限
     * @return 权限列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllPermissions() {
        List<SysPermission> permissions = sysPermissionService.getAll();
        Map<String, Object> response = new HashMap<>();
        response.put("data", permissions);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取菜单权限树
     * @return 菜单权限树
     */
    @GetMapping("/menu-tree")
    public ResponseEntity<Map<String, Object>> getMenuTree() {
        List<SysPermission> menuTree = sysPermissionService.getPermissionTree();
        Map<String, Object> response = new HashMap<>();
        response.put("data", menuTree);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取权限树
     * @return 权限树
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getPermissionTree() {
        List<SysPermission> permissionTree = sysPermissionService.getPermissionTree();
        Map<String, Object> response = new HashMap<>();
        response.put("data", permissionTree);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的菜单权限树
     * @return 用户的菜单权限树
     */
    @GetMapping("/user-menu-tree")
    public ResponseEntity<List<SysPermission>> getUserMenuTree() {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = (SysUser) authentication.getPrincipal();
        List<SysPermission> userMenuTree = sysPermissionService.getPermissionTreeByUserId(currentUser.getId());
        return ResponseEntity.ok(userMenuTree);
    }

    /**
     * 根据ID获取权限
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/get/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SysPermission> getPermissionById(@PathVariable Long id) {
        SysPermission permission = sysPermissionService.getById(id);
        return ResponseEntity.ok(permission);
    }

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建成功的权限
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SysPermission> createPermission(@RequestBody SysPermission permission) {
        SysPermission newPermission = sysPermissionService.create(permission);
        return ResponseEntity.ok(newPermission);
    }

    /**
     * 更新权限
     * @param id 权限ID
     * @param permission 更新后的权限信息
     * @return 更新后的权限
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SysPermission> updatePermission(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        SysPermission updatedPermission = sysPermissionService.update(permission);
        return ResponseEntity.ok(updatedPermission);
    }

    /**
     * 删除权限
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deletePermission(@PathVariable Long id) {
        boolean success = sysPermissionService.delete(id);
        if (success) {
            return ResponseEntity.ok("权限删除成功");
        } else {
            return ResponseEntity.badRequest().body("权限删除失败");
        }
    }
}
