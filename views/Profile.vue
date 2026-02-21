<template>
  <div class="profile-container">
    <!-- 头部 -->
    <el-header class="header">
      <h1>👤 个人中心</h1>
      <div class="header-right">
        <span class="time">{{ currentTime }}</span>
        <el-button type="primary" size="small" @click="goBack">返回首页</el-button>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边栏导航 -->
      <el-aside width="250px" class="aside">
        <el-menu :default-active="activeTab" class="profile-menu" @select="handleMenuSelect">
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="security">
            <el-icon><Lock /></el-icon>
            <span>安全设置</span>
          </el-menu-item>
          <el-menu-item index="avatar">
            <el-icon><Picture /></el-icon>
            <span>头像管理</span>
          </el-menu-item>
          <el-menu-item index="logs">
            <el-icon><Histogram /></el-icon>
            <span>登录记录</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="notifications">
            <el-icon><Message /></el-icon>
            <span>消息通知</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="badge" />
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主要内容区 -->
      <el-main class="main-content">
        <!-- 个人资料 -->
        <div v-if="activeTab === 'profile'" class="tab-content">
          <h2>个人资料</h2>
          <el-form :model="userInfo" label-width="100px" :rules="profileRules" ref="profileFormRef">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="userInfo.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="部门" prop="department">
              <el-input v-model="userInfo.department" placeholder="请输入部门" />
            </el-form-item>
            <el-form-item label="职位" prop="position">
              <el-input v-model="userInfo.position" placeholder="请输入职位" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="userInfo.role === 'admin' ? 'danger' : 'info'">
                {{ userInfo.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="注册时间">
              <span>{{ formatDate(userInfo.createdTime) }}</span>
            </el-form-item>
            <el-form-item label="最后登录">
              <span>{{ formatDate(userInfo.lastLoginTime) || '首次登录' }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="loading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 安全设置 -->
        <div v-if="activeTab === 'security'" class="tab-content">
          <h2>修改密码</h2>
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
              <el-button type="primary" @click="changePassword" :loading="loading">确认修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 头像管理 -->
        <div v-if="activeTab === 'avatar'" class="tab-content">
          <h2>头像管理</h2>
          <div class="avatar-section">
            <div class="current-avatar">
              <h3>当前头像</h3>
              <el-avatar :size="120" :src="avatarUrl || defaultAvatar" />
            </div>
            <div class="upload-avatar">
              <h3>上传新头像</h3>
              <el-upload
                  class="avatar-uploader"
                  action="http://localhost:8080/api/user/avatar"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  :before-upload="beforeAvatarUpload"
                  :headers="uploadHeaders"
              >
                <el-button type="primary">选择图片</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 jpg/png 格式，文件小于 2MB
                  </div>
                </template>
              </el-upload>
            </div>
          </div>
        </div>

        <!-- 登录记录 -->
        <div v-if="activeTab === 'logs'" class="tab-content">
          <h2>登录记录</h2>
          <el-table :data="loginLogs" stripe style="width: 100%">
            <el-table-column prop="loginTime" label="登录时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.loginTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="ipAddress" label="IP地址" width="150" />
            <el-table-column prop="userAgent" label="设备信息" show-overflow-tooltip />
            <el-table-column prop="loginStatus" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.loginStatus === 'success' ? 'success' : 'danger'">
                  {{ row.loginStatus }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'favorites'" class="tab-content">
          <h2>我的收藏</h2>
          <el-tabs v-model="favoriteType">
            <el-tab-pane label="故障知识" name="fault">
              <el-table :data="favorites" stripe style="width: 100%">
                <el-table-column prop="faultType" label="故障类型" width="180" />
                <el-table-column prop="faultLevel" label="等级" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getFaultLevelType(row.faultLevel)">
                      {{ row.faultLevel }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="note" label="备注" />
                <el-table-column prop="createdTime" label="收藏时间" width="180">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createdTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button type="danger" size="small" @click="deleteFavorite(row.id)">取消</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 消息通知 -->
        <div v-if="activeTab === 'notifications'" class="tab-content">
          <h2>
            消息通知
            <el-button v-if="unreadCount > 0" type="primary" size="small" @click="markAllRead" style="margin-left: 20px;">
              全部已读
            </el-button>
          </h2>
          <el-timeline>
            <el-timeline-item
                v-for="(item, index) in notifications"
                :key="index"
                :type="item.isRead ? 'info' : 'primary'"
                :timestamp="formatDateTime(item.createdTime)"
            >
              <el-card :class="{ 'unread': !item.isRead }">
                <div class="notification-item" @click="markAsRead(item)">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.content }}</p>
                  <el-tag size="small" :type="getNotificationType(item.type)">
                    {{ item.type }}
                  </el-tag>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Picture, Histogram, Star, Message } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('profile')
const currentTime = ref(new Date().toLocaleString())
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = ref({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  position: '',
  role: '',
  avatar: '',
  createdTime: '',
  lastLoginTime: ''
})

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 登录记录
const loginLogs = ref([])

// 收藏
const favoriteType = ref('fault')
const favorites = ref([])

// 消息通知
const notifications = ref([])
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.isRead).length
})

// 头像上传
const avatarUrl = ref('')
const uploadHeaders = {
  Authorization: localStorage.getItem('token') || ''
}

// 表单验证规则
const profileRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息（从本地存储）
const fetchUserInfo = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const userData = JSON.parse(userStr)
      userInfo.value = {
        id: userData.id || '',
        username: userData.username || '',
        realName: userData.realName || '',
        email: userData.email || '',
        phone: userData.phone || '',
        department: userData.department || '',
        position: userData.position || '',
        role: userData.role || 'user',
        avatar: userData.avatar || '',
        createdTime: userData.createdTime || '',
        lastLoginTime: userData.lastLoginTime || ''
      }
      avatarUrl.value = userData.avatar || ''
      console.log('从本地存储加载用户信息:', userInfo.value)
    } else {
      // 如果没有本地存储，调用后端
      await fetchUserInfoFromBackend()
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 从后端获取用户信息
const fetchUserInfoFromBackend = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('http://localhost:8080/api/user/info', {
      headers: { 'Authorization': token }
    })
    if (response.data.code === 200) {
      userInfo.value = response.data.data
      avatarUrl.value = userInfo.value.avatar
      // 保存到本地存储
      localStorage.setItem('user', JSON.stringify(userInfo.value))
    }
  } catch (error) {
    console.error('从后端获取用户信息失败:', error)
  }
}

// 更新个人资料（模拟版）
const updateProfile = async () => {
  console.log('开始更新个人资料')

  loading.value = true

  try {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 更新本地存储
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      user.realName = userInfo.value.realName
      user.email = userInfo.value.email
      user.phone = userInfo.value.phone
      user.department = userInfo.value.department
      user.position = userInfo.value.position
      localStorage.setItem('user', JSON.stringify(user))
    }

    // 更新 localStorage 中的单独字段
    localStorage.setItem('username', userInfo.value.realName || userInfo.value.username)

    // 显示成功消息
    ElMessage.success('个人资料更新成功')

    // 重新获取用户信息（从本地存储）
    await fetchUserInfo()

  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

// 修改密码
const changePassword = async () => {
  console.log('开始修改密码')
  console.log('密码表单:', passwordForm.value)

  // 表单验证
  if (!passwordForm.value.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少6位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }

  loading.value = true
  try {
    const token = localStorage.getItem('token')

    const passwordData = {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    }

    console.log('发送数据:', passwordData)

    const response = await axios.post('http://localhost:8080/api/user/change-password', passwordData, {
      headers: { 'Authorization': token }
    })

    console.log('响应数据:', response.data)

    if (response.data.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')

      // 清除本地存储
      localStorage.clear()

      // 延迟跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(response.data.message || '修改失败')
    }
  } catch (error) {
    console.error('修改密码错误:', error)
    ElMessage.error('修改失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 获取登录日志
const fetchLoginLogs = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/user/login-logs', {
      headers: { Authorization: localStorage.getItem('token') }
    })
    if (response.data.code === 200) {
      loginLogs.value = response.data.data
    }
  } catch (error) {
    console.error('获取登录日志失败:', error)
  }
}

// 获取收藏
const fetchFavorites = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/user/favorites', {
      headers: { Authorization: localStorage.getItem('token') }
    })
    if (response.data.code === 200) {
      favorites.value = response.data.data
    }
  } catch (error) {
    console.error('获取收藏失败:', error)
  }
}

// 删除收藏
const deleteFavorite = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`http://localhost:8080/api/user/favorites/${id}`, {
      headers: { Authorization: localStorage.getItem('token') }
    })
    if (response.data.code === 200) {
      ElMessage.success('取消成功')
      fetchFavorites()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 获取消息
const fetchNotifications = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/user/notifications', {
      headers: { Authorization: localStorage.getItem('token') }
    })
    if (response.data.code === 200) {
      notifications.value = response.data.data
    }
  } catch (error) {
    console.error('获取消息失败:', error)
  }
}

// 标记已读
const markAsRead = async (item) => {
  if (!item.isRead) {
    try {
      await axios.put(`http://localhost:8080/api/user/notifications/${item.id}/read`, {}, {
        headers: { Authorization: localStorage.getItem('token') }
      })
      item.isRead = true
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

// 全部已读
const markAllRead = async () => {
  try {
    await axios.put('http://localhost:8080/api/user/notifications/read-all', {}, {
      headers: { Authorization: localStorage.getItem('token') }
    })
    notifications.value.forEach(n => n.isRead = true)
    ElMessage.success('全部已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    avatarUrl.value = response.data
    userInfo.value.avatar = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message)
  }
}

// 头像上传失败
const handleAvatarError = () => {
  ElMessage.error('上传失败')
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG/PNG 格式图片')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
  }
  return isJpgOrPng && isLt2M
}

// 菜单切换
const handleMenuSelect = (index) => {
  activeTab.value = index
  switch (index) {
    case 'logs':
      fetchLoginLogs()
      break
    case 'favorites':
      fetchFavorites()
      break
    case 'notifications':
      fetchNotifications()
      break
  }
}

// 返回首页
const goBack = () => {
  router.push('/')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取故障等级样式
const getFaultLevelType = (level) => {
  const map = { 'high': 'danger', 'medium': 'warning', 'low': 'info' }
  return map[level] || 'info'
}

// 获取消息类型样式
const getNotificationType = (type) => {
  const map = { 'system': 'info', 'fault': 'danger', 'warning': 'warning' }
  return map[type] || 'info'
}

onMounted(() => {
  fetchUserInfo()
  setInterval(() => {
    currentTime.value = new Date().toLocaleString()
  }, 1000)
})
</script>

<style scoped>
.profile-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.time {
  font-size: 1rem;
  opacity: 0.9;
}

.aside {
  background: white;
  border-right: 1px solid #e4e7ed;
  padding: 20px 0;
}

.profile-menu {
  border-right: none;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
}

.tab-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.tab-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 1.2rem;
  border-bottom: 2px solid #2a5298;
  padding-bottom: 10px;
}

.avatar-section {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.current-avatar {
  text-align: center;
}

.current-avatar h3 {
  margin-bottom: 15px;
  font-size: 1rem;
  color: #606266;
}

.upload-avatar {
  flex: 1;
}

.upload-avatar h3 {
  margin-bottom: 15px;
  font-size: 1rem;
  color: #606266;
}

.notification-item {
  cursor: pointer;
}

.notification-item h4 {
  margin: 0 0 8px 0;
  font-size: 1rem;
}

.notification-item p {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 0.9rem;
}

.unread {
  background-color: #ecf5ff;
}

.badge {
  margin-left: auto;
}

.el-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.el-form {
  max-width: 500px;
}
</style>