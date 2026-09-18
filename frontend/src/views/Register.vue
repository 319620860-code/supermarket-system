<template>
  <div class="register-container">
    <el-card class="register-card" shadow="hover">
      <template #header>
        <div class="register-header">
          <h2>超市信息管理系统</h2>
          <p>注册新账号</p>
          <p class="register-subhint">注册仅创建账号；登录身份需管理员在「用户管理」中分配角色。</p>
        </div>
      </template>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            clearable
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="el-icon-lock"
            clearable
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            prefix-icon="el-icon-user"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="el-icon-phone"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="el-icon-message"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            @click="handleRegister"
            :loading="loading"
          >
            注册
          </el-button>
          <el-button
            size="large"
            class="login-btn"
            @click="goToLogin"
          >
            返回登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { authAPI } from '../services/api'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: ''
})

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 处理注册
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    console.log('注册表单数据:', registerForm)
    
    // 发送注册请求
    const response = await authAPI.register({
      username: registerForm.username,
      password: registerForm.password,
      realName: registerForm.realName,
      phone: registerForm.phone,
      email: registerForm.email
    })
    
    console.log('注册响应:', response)

    const ok = response?.success === true || response?.data?.success === true
    const msg = response?.message ?? response?.data?.message
    if (ok) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(msg || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
    if (error.response) {
      // 服务器返回了错误响应
      if (error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else if (error.response.data) {
        ElMessage.error(JSON.stringify(error.response.data))
      } else {
        ElMessage.error(`注册失败: ${error.response.status}`)
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('注册失败，服务器未响应，请检查网络连接')
    } else {
      // 请求配置出错
      ElMessage.error(`注册失败: ${error.message}`)
    }
  } finally {
    loading.value = false
  }
}

// 返回登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: var(--sm-gradient-page, linear-gradient(145deg, #0f766e 0%, #134e4a 35%, #0c4a6e 100%));
  background-size: cover;
  background-position: center;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 500px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
}

.register-header {
  text-align: center;
  padding: 30px 25px 20px;
  background: var(--sm-gradient-hero, linear-gradient(135deg, #14b8a6 0%, #0d9488 45%, #047857 100%));
  color: white;
}

.register-header h2 {
  margin-bottom: 10px;
  font-size: 28px;
  font-weight: 600;
}

.register-header p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.register-subhint {
  margin-top: 12px !important;
  font-size: 12px !important;
  line-height: 1.5;
  opacity: 0.85;
  max-width: 360px;
  margin-left: auto;
  margin-right: auto;
}

.register-form {
  padding: 30px 25px;
  background-color: white;
  width: 100%;
  box-sizing: border-box;
}

/* 表单项目间距 */
.el-form-item {
  margin-bottom: 22px;
}

/* 标签样式 */
.el-form-item__label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

/* 输入框样式 */
.el-input__wrapper {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-input__wrapper:hover {
  box-shadow: 0 2px 8px rgba(13, 148, 136, 0.2);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(13, 148, 136, 0.35);
}

/* 选择框样式 */
.el-select .el-input__wrapper {
  border-radius: 8px;
}

/* 按钮容器 */
.el-form-item:last-child {
  margin-bottom: 0;
  padding-top: 10px;
}

/* 按钮样式 */
.register-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: var(--sm-gradient-btn, linear-gradient(135deg, #2dd4bf 0%, #0d9488 55%, #0f766e 100%));
  border: none;
  transition: all 0.3s ease;
  margin-bottom: 12px;
  box-sizing: border-box;
  color: #fff;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 22px rgba(13, 148, 136, 0.4);
  filter: brightness(1.02);
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-card {
    margin: 0 20px;
  }
  
  .register-form {
    padding: 25px 20px;
  }
  
  .register-header h2 {
    font-size: 24px;
  }
}
</style>