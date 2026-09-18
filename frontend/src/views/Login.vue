<template>
  <div class="login-container">
    <!-- 背景遮罩层 -->
    <div class="background-overlay"></div>
    <!-- 装饰性背景元素 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
    
    <div class="login-content">
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="login-header">
            <div class="system-logo">
              <el-icon class="logo-icon"><ShoppingCart /></el-icon>
            </div>
            <h2>超市信息管理系统</h2>
            <p>欢迎回来，请登录您的账号</p>
          </div>
        </template>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          label-position="top"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="你的身份是：">
            <div class="role-selection">
              <el-radio-group v-model="loginForm.role" size="large">
                <el-radio-button label="admin">
                  <el-icon class="role-icon"><Management /></el-icon>
                  管理员
                </el-radio-button>
                <el-radio-button label="cashier">
                  <el-icon class="role-icon"><Money /></el-icon>
                  收银员
                </el-radio-button>
                <el-radio-button label="stock">
                  <el-icon class="role-icon"><Box /></el-icon>
                  库存管理员
                </el-radio-button>
              </el-radio-group>
            </div>
          </el-form-item>
          
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              clearable
              :class="{ 'input-focus': inputFocus.username }"
              @focus="inputFocus.username = true"
              @blur="inputFocus.username = false"
            ></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              clearable
              show-password
              :class="{ 'input-focus': inputFocus.password }"
              @focus="inputFocus.password = true"
              @blur="inputFocus.password = false"
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              native-type="button"
              class="login-btn"
              @click="handleLogin"
              :loading="loading"
            >
              <el-icon class="btn-icon"><Right /></el-icon>
              登录
            </el-button>
            <div class="register-link">
              还未注册？<span @click="goToRegister">立即注册</span>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { authAPI } from '../services/api'
import { 
  ShoppingCart, 
  Management, 
  Money, 
  Box, 
  Right 
} from '@element-plus/icons-vue'

// 使用纯CSS渐变背景，无需外部图片
const backgroundImage = ''

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)
const inputFocus = reactive({
  username: false,
  password: false
})

// 页面加载动画
onMounted(() => {
  const container = document.querySelector('.login-container')
  container?.classList.add('loaded')
})

const loginForm = reactive({
  username: 'admin',
  password: '123456',
  role: 'admin'
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    console.log('登录请求数据:', loginForm)
    
    // 构建登录请求数据（包含用户名、密码和选择的角色）
    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      role: loginForm.role
    }
    
    // 发送登录请求
    const response = await authAPI.login(loginData)
    
    console.log('登录响应:', response)
    
    // 保存完整的用户信息到localStorage
    localStorage.setItem('token', response.token)
    localStorage.setItem('user', JSON.stringify(response.user || {}))
    localStorage.setItem('roles', JSON.stringify(response.roles || []))
    localStorage.setItem('selectedRole', response.selectedRole || loginForm.role)
    
    ElMessage.success('登录成功')
    router.push('/home/dashboard')
  } catch (error) {
    console.error('登录错误:', error)
    if (error.response) {
      console.error('响应错误:', error.response)
      const d = error.response.data
      const msg = typeof d === 'string' && d ? d : (d?.message || '登录失败')
      ElMessage.error(msg)
    } else if (error.request) {
      console.error('请求错误:', error.request)
      ElMessage.error('登录失败，服务器无响应')
    } else {
      console.error('其他错误:', error.message)
      ElMessage.error(error.message || '登录失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* 全局动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 登录容器 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: url('https://picsum.photos/1920/1080') no-repeat center center fixed;
  background-size: cover;
  padding: 20px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transition: opacity 0.8s ease;
}

/* 背景遮罩层 */
.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.75) 0%, rgba(19, 78, 74, 0.8) 40%, rgba(12, 74, 110, 0.85) 100%);
  z-index: 0;
}

.login-container.loaded {
  opacity: 1;
}

/* 背景装饰元素 */
.background-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -150px;
  right: -150px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 4s;
}

/* 登录内容 */
.login-content {
  z-index: 1;
  width: 100%;
  max-width: 520px;
  animation: fadeInUp 0.8s ease-out 0.3s both;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.4);
}

/* 登录头部 */
.login-header {
  text-align: center;
  padding: 40px 30px 30px;
  background: var(--sm-gradient-hero, linear-gradient(135deg, #14b8a6 0%, #0d9488 45%, #047857 100%));
  color: white;
}

.system-logo {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  animation: pulse 2s ease-in-out infinite;
}

.logo-icon {
  font-size: 40px;
  color: white;
}

.login-header h2 {
  margin-bottom: 12px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 1px;
}

.login-header p {
  margin: 0;
  font-size: 15px;
  opacity: 0.95;
  font-weight: 400;
}

/* 登录表单 */
.login-form {
  padding: 40px 30px 35px;
  background-color: white;
  width: 100%;
  box-sizing: border-box;
}

/* 表单项目间距 */
.el-form-item {
  margin-bottom: 28px;
  animation: fadeInUp 0.6s ease-out both;
}

.el-form-item:nth-child(1) { animation-delay: 0.5s; }
.el-form-item:nth-child(2) { animation-delay: 0.6s; }
.el-form-item:nth-child(3) { animation-delay: 0.7s; }
.el-form-item:nth-child(4) { animation-delay: 0.8s; }

/* 角色选择样式 */
.role-selection {
  display: flex;
  justify-content: center;
  margin-top: 0;
}

.el-radio-group {
  width: 100%;
  display: flex;
  gap: 2px;
}

.el-radio-button {
  flex: 1;
  margin: 0;
  transition: all 0.3s ease;
}

.el-radio-button:first-child .el-radio-button__inner {
  border-radius: 12px 0 0 12px;
  border-left: 1px solid #e0e0e0;
}

.el-radio-button:last-child .el-radio-button__inner {
  border-radius: 0 12px 12px 0;
}

.el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: var(--sm-gradient-hero, linear-gradient(135deg, #14b8a6 0%, #0d9488 45%, #047857 100%));
  border-color: var(--sm-primary, #0d9488);
  color: white;
  box-shadow: 0 4px 14px rgba(13, 148, 136, 0.45);
  transform: translateY(-1px);
}

.el-radio-button__inner {
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 46px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #e0e0e0;
  background-color: #fafafa;
  color: #555;
  border-radius: 0;
}

.el-radio-button:hover .el-radio-button__inner {
  border-color: var(--sm-primary-light, #14b8a6);
  background-color: var(--sm-primary-soft, #ccfbf1);
  color: var(--sm-primary-dark, #0f766e);
}

.role-icon {
  font-size: 16px;
}

/* 标签样式 */
.el-form-item__label {
  font-weight: 600;
  color: #333;
  font-size: 15px;
  margin-bottom: 8px;
  display: block;
}

/* 输入框样式 */
.el-input {
  width: 100%;
}

.el-input__wrapper {
  border-radius: 12px;
  transition: all 0.3s ease;
  height: 46px;
  border: 1px solid #e0e0e0;
  box-shadow: none;
}

.el-input__wrapper:hover {
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.18);
  border-color: var(--sm-primary, #0d9488);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.22);
  border-color: var(--sm-primary, #0d9488);
}

.el-input__inner {
  font-size: 15px;
  color: #333;
  height: 44px;
  line-height: 44px;
}

.el-input__prefix {
  left: 14px;
  color: #999;
}

.el-input__suffix {
  right: 14px;
}

/* 按钮容器 */
.el-form-item:last-child {
  margin-bottom: 0;
  padding-top: 8px;
}

/* 按钮样式 */
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: var(--sm-gradient-btn, linear-gradient(135deg, #2dd4bf 0%, #0d9488 55%, #0f766e 100%));
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 16px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 16px rgba(13, 148, 136, 0.35);
  color: white;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(13, 148, 136, 0.45);
  filter: brightness(1.03);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 14px rgba(13, 148, 136, 0.35);
}

.btn-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.login-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 18px;
  font-size: 14px;
  color: #666;
  font-weight: 400;
}

.register-link span {
  color: var(--sm-primary, #0d9488);
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 600;
  position: relative;
  padding: 0 4px;
}

.register-link span::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background: var(--sm-primary, #0d9488);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.register-link span:hover {
  color: var(--sm-primary-dark, #0f766e);
}

.register-link span:hover::after {
  transform: scaleX(1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
  }
  
  .login-card {
    margin: 0;
    border-radius: 16px;
  }
  
  .login-header {
    padding: 35px 25px 25px;
  }
  
  .system-logo {
    width: 70px;
    height: 70px;
    margin-bottom: 18px;
  }
  
  .logo-icon {
    font-size: 35px;
  }
  
  .login-header h2 {
    font-size: 28px;
  }
  
  .login-form {
    padding: 35px 25px 30px;
  }
  
  .el-form-item {
    margin-bottom: 25px;
  }
  
  .el-input__wrapper, .el-radio-button__inner, .login-btn {
    height: 44px;
  }
  
  .el-input__inner {
    height: 42px;
    line-height: 42px;
    font-size: 14px;
  }
  
  .login-btn {
    font-size: 15px;
  }
  
  .decoration-circle {
    transform: scale(0.7);
  }
}

@media (max-width: 480px) {
  .login-header h2 {
    font-size: 24px;
  }
  
  .login-header p {
    font-size: 14px;
  }
  
  .login-form {
    padding: 30px 20px 25px;
  }
  
  .el-form-item {
    margin-bottom: 22px;
  }
  
  .el-form-item__label {
    font-size: 14px;
  }
}
</style>