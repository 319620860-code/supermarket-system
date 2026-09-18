package com.supermarket.backend.controller;

import com.supermarket.backend.entity.SysPermission;
import com.supermarket.backend.entity.SysRole;
import com.supermarket.backend.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 获取所有角色
     * @return 角色列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        List<SysRole> roles = sysRoleService.getAll();
        Map<String, Object> response = new HashMap<>();
        response.put("data", roles);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", role);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建成功的角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody SysRole role) {
        SysRole newRole = sysRoleService.create(role);
        Map<String, Object> response = new HashMap<>();
        response.put("data", newRole);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新角色
     * @param id 角色ID
     * @param role 更新后的角色信息
     * @return 更新后的角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        SysRole updatedRole = sysRoleService.update(role);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedRole);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        boolean success = sysRoleService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", success);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取角色的权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getRolePermissions(@PathVariable Long roleId) {
        List<SysPermission> permissionList = sysRoleService.getRolePermissions(roleId);
        List<Long> permissionIds = permissionList.stream().map(SysPermission::getId).collect(java.util.stream.Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("data", permissionIds);
        return ResponseEntity.ok(response);
    }

    /**
     * 设置角色的权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 设置结果
     */
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> setRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        boolean success = sysRoleService.setPermissions(roleId, permissionIds);
        Map<String, Object> response = new HashMap<>();
        response.put("data", success);
        return ResponseEntity.ok(response);
    }
}


