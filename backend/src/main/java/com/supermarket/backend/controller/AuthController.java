package com.supermarket.backend.controller;

import com.supermarket.backend.entity.SysRole;
import com.supermarket.backend.entity.SysUser;
import com.supermarket.backend.service.SysUserService;
import com.supermarket.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 执行登录验证（包含角色验证）
            SysUser user = sysUserService.login(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.getRole());
            
            // 获取用户的所有角色
            List<SysRole> userRoles = sysUserService.getUserRoles(user.getId());
            List<String> roleCodes = userRoles.stream()
                .map(SysRole::getCode)
                .collect(java.util.stream.Collectors.toList());
            
            // 生成包含角色信息的JWT令牌
            String token = jwtUtil.generateToken(user.getUsername(), roleCodes, loginRequest.getRole());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("roles", roleCodes);
            response.put("selectedRole", loginRequest.getRole()); // 返回用户选择的角色
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 账号/密码/身份等业务错误用 400，避免前端 axios 把 401 当作「未登录」整页跳转登录，导致看不到提示
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     * @param user 当前登录用户
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<SysUser> getCurrentUser(@RequestAttribute("currentUser") SysUser user) {
        return ResponseEntity.ok(user);
    }

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 检查用户名是否已存在
            if (sysUserService.getByUsername(registerRequest.getUsername()) != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名已存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 仅创建账号；角色由管理员在「用户管理」中分配
            SysUser newUser = new SysUser();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(registerRequest.getPassword());
            newUser.setRealName(registerRequest.getRealName());
            newUser.setPhone(registerRequest.getPhone());
            newUser.setEmail(registerRequest.getEmail());
            
            SysUser createdUser = sysUserService.create(newUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;
        private String role;

        // getter and setter
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
    
    /**
     * 注册请求DTO
     */
    public static class RegisterRequest {
        private String username;
        private String password;
        private String realName;
        private String phone;
        private String email;

        // getter and setter
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    /**
     * 临时端点：生成BCrypt哈希值（用于测试）
     */
    @PostMapping("/generate-hash")
    public ResponseEntity<?> generateHash(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        if (password == null) {
            return ResponseEntity.badRequest().body("Password parameter is required");
        }
        String hash = passwordEncoder.encode(password);
        Map<String, String> response = new HashMap<>();
        response.put("hash", hash);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 修改密码
     * @param changePasswordRequest 修改密码请求
     * @param user 当前登录用户
     * @return 修改密码响应
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, @RequestAttribute("currentUser") SysUser user) {
        try {
            // 验证旧密码是否正确
            if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword()) && !user.getPassword().equals(changePasswordRequest.getOldPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("原密码错误");
            }
            
            // 验证新密码长度
            if (changePasswordRequest.getNewPassword().length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("新密码长度不能少于6位");
            }
            
            // 更新密码
            user.setPassword(changePasswordRequest.getNewPassword());
            sysUserService.update(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "密码修改失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 修改密码请求DTO
     */
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        // getter and setter
        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

