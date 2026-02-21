<template>
  <div class="login-container">
    <div class="login-card">
      <h2>船舶运行多智能体系统</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" @keyup.enter="handleLogin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="密码" prefix-icon="Lock" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" />
            </el-form-item>
            <el-form-item prop="realName">
              <el-input v-model="registerForm.realName" placeholder="真实姓名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="registerForm.email" placeholder="邮箱" prefix-icon="Message" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }]
}

const handleLogin = async () => {
  loading.value = true
  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', loginForm.value)
    if (response.data.code === 200) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('userId', response.data.userId)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('role', response.data.role)
      localStorage.setItem('user', JSON.stringify(response.data))
      ElMessage.success('登录成功')
      router.push('/')
    }else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('登录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  loading.value = true
  try {
    const response = await axios.post('http://localhost:8080/api/auth/register', registerForm.value)
    if (response.data.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      registerForm.value = { username: '', password: '', confirmPassword: '', realName: '', email: '' }
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('注册失败：' + error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.user-dropdown {
  cursor: pointer;
  color: white;
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-dropdown:hover {
  opacity: 0.8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #1e3c72;
}
</style>