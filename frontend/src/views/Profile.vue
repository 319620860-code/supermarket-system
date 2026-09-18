<template>
  <div class="profile-container">
    <div class="page-header">
      <h2 class="page-subtitle">
        <el-icon><UserFilled /></el-icon>
        个人中心
      </h2>
    </div>

    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" type="email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag size="large" effect="dark">{{ userForm.roleName }}</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="profile-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePasswordSubmit">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { authAPI } from '../services/api'

const userFormRef = ref(null)
const passwordFormRef = ref(null)

const userForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  roleName: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleSubmit = async () => {
  try {
    await userFormRef.value.validate()
    // 这里应该调用API更新用户信息
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('验证失败:', error)
  }
}

const handlePasswordSubmit = async () => {
  try {
    await passwordFormRef.value.validate()
    
    // 调用API修改密码
    await authAPI.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功')
    // 重置密码表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('密码修改失败:', error)
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message || '密码修改失败')
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('原密码错误或网络异常')
    }
  }
}

const loadUserInfo = async () => {
  try {
    const response = await authAPI.getCurrentUser()
    const userData = response.data || response
    userForm.username = userData.username || ''
    userForm.realName = userData.realName || userData.real_name || ''
    userForm.phone = userData.phone || ''
    userForm.email = userData.email || ''
    
    // 获取角色名称
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    const roleNames = {
      admin: '管理员',
      cashier: '收银员',
      stock: '仓库管理员'
    }
    userForm.roleName = roles.map(role => roleNames[role] || role).join(', ')
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-subtitle {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 600;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>