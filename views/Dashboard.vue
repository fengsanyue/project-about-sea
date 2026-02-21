<template>
  <div class="dashboard">
    <!-- 头部 -->
    <el-header class="header">
      <h1>🚢 船舶运行多智能体系统故障诊断平台</h1>
      <div class="header-right">
        <span class="time">{{ currentTime }}</span>
        <el-dropdown @command="handleCommand">
      <span class="user-dropdown">
        {{ username }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
      </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 左侧主要内容区 -->
      <el-main class="main-content">
        <el-row :gutter="16">
          <!-- 船员出勤图表 -->
          <el-col :span="24" class="chart-card">
            <div class="card-header">
              <span class="card-title">👥 船员出勤记录</span>
              <span class="card-subtitle">总出勤工时: 2450h | 出勤次数: 128次</span>
            </div>
            <div class="chart-container" ref="attendanceChart"></div>
          </el-col>

          <!-- 参数轮播 -->
          <el-col :span="24" class="param-card">
            <el-carousel height="60px" direction="vertical" :autoplay="true" :interval="3000">
              <el-carousel-item v-for="item in paramList" :key="item.name">
                <div class="param-item">
                  <span class="param-name">{{ item.name }}</span>
                  <span class="param-value">{{ item.value }}</span>
                  <span class="param-unit">{{ item.unit }}</span>
                </div>
              </el-carousel-item>
            </el-carousel>
          </el-col>

          <!-- 主机实时参数 -->
          <el-col :span="12" class="table-card">
            <div class="card-header">⚙️ 主机实时参数</div>
            <el-table :data="engineParams" stripe size="small" style="width: 100%">
              <el-table-column prop="name" label="参数" width="100"></el-table-column>
              <el-table-column prop="standard" label="标准" width="120"></el-table-column>
              <el-table-column prop="current" label="实时">
                <template #default="{ row }">
                  <span :class="getParamStatus(row)">{{ row.current }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-col>

          <!-- 最优航速 -->
          <el-col :span="12" class="speed-card">
            <div class="card-header">⚡ 最优航速建议</div>
            <div class="speed-list">
              <div v-for="speed in optimalSpeeds" :key="speed.value"
                   class="speed-item" :class="{ active: speed.value === currentSpeed }">
                <span class="speed-value">{{ speed.value }}</span>
                <span class="speed-label">{{ speed.label }}</span>
              </div>
            </div>
            <div class="speed-advice">{{ fuelAdvice }}</div>
          </el-col>

          <!-- 故障列表 -->
          <el-col :span="24" class="fault-card">
            <div class="card-header">⚠️ 最近故障诊断</div>
            <el-table :data="faultList" stripe size="small" style="width: 100%">
              <el-table-column prop="faultType" label="故障类型" width="150"></el-table-column>
              <el-table-column prop="faultLevel" label="等级" width="80">
                <template #default="{ row }">
                  <el-tag :type="getFaultLevelType(row.faultLevel)">{{ row.faultLevel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述"></el-table-column>
              <el-table-column prop="detectedTime" label="检测时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.detectedTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="isSolved" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.isSolved === 1 ? 'success' : 'danger'">
                    {{ row.isSolved === 1 ? '已解决' : '未解决' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </el-main>

      <!-- 右侧AI助手区 -->
      <el-aside width="350px" class="aside">
        <!-- AI助手卡片 -->
        <el-card class="ai-card">
          <!-- 头部 -->
          <template #header>
            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <el-icon><ChatDotRound /></el-icon>
                <span>🤖 AI船舶运行助手小Xiao</span>
              </div>
              <!-- 停止播报按钮 -->
              <div v-if="isSpeaking">
                <el-button
                    type="danger"
                    size="small"
                    circle
                    @click="stopSpeaking"
                    title="停止语音播报"
                >
                  <el-icon><Mute /></el-icon>
                </el-button>
              </div>
            </div>
          </template>

          <!-- 对话区域 -->
          <div class="chat-messages" ref="chatMessagesDiv">
            <div v-for="(msg, index) in chatMessages" :key="index"
                 :class="['message', msg.type]">
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
            <div v-if="aiThinking" class="message ai">
              <div class="message-content">...</div>
            </div>
          </div>

          <!-- 输入区域（带语音按钮） -->
          <div class="chat-input-wrapper">
            <div class="chat-input">
              <el-input
                  v-model="userInput"
                  placeholder="请输入您的问题..."
                  @keydown.enter.prevent="sendMessage"
                  :disabled="aiThinking"
              >
                <template #prepend>
                  <el-button
                      :icon="Microphone"
                      :type="isListening ? 'danger' : 'default'"
                      @click="toggleVoiceInput"
                      :loading="isListening"
                  />
                </template>
                <template #append>
                  <el-button @click="sendMessage" :loading="aiThinking">发送</el-button>
                </template>
              </el-input>
            </div>

            <!-- 语音状态提示 -->
            <transition name="fade">
              <div class="voice-status" v-if="isListening">
                <el-tag type="danger" effect="dark" size="large">
                  <el-icon class="is-loading"><Microphone /></el-icon>
                  正在聆听您的指令...
                </el-tag>
                <p class="voice-hint">点击语音按钮停止聆听</p>
                <!-- 临时调试信息 -->
                <div style="color: red; font-size: 12px;">
                  调试: isListening = {{ isListening }}
                </div>
              </div>
            </transition>
          </div>

          <!-- 自动发送选项 -->
          <div class="auto-send-option">
            <el-checkbox v-model="autoSendAfterVoice">识别后自动发送</el-checkbox>
          </div>

          <!-- 快捷入口 -->
          <div class="quick-actions">
            <el-button size="small" @click="quickQuestion('当前温度正常吗？')">🌡️ 温度查询</el-button>
            <el-button size="small" @click="quickQuestion('当前油耗多少？')">⛽ 油耗查询</el-button>
            <el-button size="small" @click="quickQuestion('有故障吗？')">⚠️ 故障查询</el-button>
            <el-button size="small" @click="quickQuestion('航速建议')">⚡ 航速建议</el-button>
          </div>
        </el-card>
        <!-- 结束 AI助手卡片 -->

        <!-- 仓库使用情况 -->
        <el-card class="storage-card">
          <template #header>
            <div class="card-header">
              <el-icon><Box /></el-icon>
              <span>📦 仓库使用情况</span>
            </div>
          </template>
          <div class="storage-list">
            <div v-for="item in storageList" :key="item.name" class="storage-item">
              <span>{{ item.name }}</span>
              <el-progress :percentage="item.percentage" :color="item.color" :stroke-width="15" />
            </div>
          </div>
        </el-card>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup>
import axios from 'axios'
import { ref, onMounted, onUnmounted, nextTick} from 'vue'
import { ChatDotRound, Box, Microphone, Close, Mute } from '@element-plus/icons-vue'
import { shipApi } from '../api'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()

// 从 localStorage 获取用户信息
const username = ref(localStorage.getItem('username') || '用户')
const userId = ref(localStorage.getItem('userId'))
// 直接显示原始角色名
const userRole = ref(localStorage.getItem('role') || 'admin')

// 下拉菜单命令处理
const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/profile')  // 跳转到个人中心
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const token = localStorage.getItem('token')
    if (token) {
      // 调用后端注销接口
      await axios.post('http://localhost:8080/api/auth/logout', {}, {
        headers: { 'Authorization': token }
      })
    }

    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('user')

    ElMessage.success('已退出登录')
    router.push('/login')

  } catch (error) {
    // 用户取消操作，不处理
    if (error !== 'cancel') {
      console.error('退出失败:', error)
    }
  }
}

// ==================== 响应式数据 ====================
const currentTime = ref(new Date().toLocaleString())
const analyzing = ref(false)
// AI思考状态
const aiThinking = ref(false)
const faultList = ref([])
const chatMessages = ref([
  { type: 'ai', content: '您好，我是AI船舶运行助手，有什么可以帮您？', time: '刚刚' }
])
// 语音播报状态
const isSpeaking = ref(false)
let currentUtterance = null
// 添加自动发送开关
// 测试方法 - 手动设置输入框的值
const testSetInput = () => {
  userInput.value = '这是测试文本'
  console.log('手动设置后:', userInput.value)
}
const autoSendAfterVoice = ref(false)
// 语音识别相关
const isListening = ref(false)
const recognition = ref(null)
const userInput = ref('')

// 初始化语音识别
onMounted(() => {
  console.log('组件挂载，初始化语音识别...')

  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    recognition.value = new SpeechRecognition()

    recognition.value.continuous = false
    recognition.value.interimResults = true
    recognition.value.lang = 'zh-CN'

    recognition.value.onresult = (event) => {
      const transcript = event.results[0][0].transcript
      console.log('最终识别结果:', transcript)
      userInput.value = transcript

      if (autoSendAfterVoice.value) {
        // 延迟500ms发送，让用户看到结果
        setTimeout(() => {
          sendMessage()
          ElMessage.success(`已发送: ${transcript}`)
        }, 500)
      } else {
        ElMessage.info(`识别到: ${transcript}，按回车发送`)
      }
    }

    recognition.value.onend = () => {
      console.log('语音识别结束')
      // 确保这里把 isListening 设为 false
      isListening.value = false
      console.log('isListening 已重置为:', isListening.value)

      // 关闭所有提示消息
      ElMessage.closeAll()

      if (userInput.value) {
        ElMessage.success(`识别完成: ${userInput.value}`)
      }
    }

    recognition.value.onerror = (event) => {
      console.error('语音识别错误:', event.error)

      // 发生错误时也要重置
      isListening.value = false
      ElMessage.closeAll()

      if (event.error !== 'no-speech') {
        ElMessage.error('语音识别失败: ' + event.error)
      }
    }

    recognition.value.onsoundstart = () => {
      console.log('检测到声音')
    }

    console.log('语音识别初始化成功')
  } else {
    ElMessage.warning('当前浏览器不支持语音识别，请使用Chrome或Edge')
  }
  window.addEventListener('keydown', handleGlobalKeyDown)
})

// 切换语音输入
const toggleVoiceInput = () => {
  console.log('切换语音输入，当前状态:', isListening.value)

  if (!recognition.value) {
    ElMessage.warning('您的浏览器不支持语音识别')
    return
  }

  if (isListening.value) {
    // 正在聆听，点击后停止
    recognition.value.stop()
    isListening.value = false
    ElMessage.closeAll()
    ElMessage.info('语音识别已停止')
  } else {
    try {
      // 开始聆听
      recognition.value.start()
      isListening.value = true

      // 显示正在聆听的提示
      const message = ElMessage({
        message: '🎤 正在聆听... 请说话',
        type: 'info',
        duration: 0,
        showClose: true
      })

      console.log('语音识别已启动，isListening =', isListening.value)

      // 设置一个超时，10秒后自动停止（防止一直显示）
      setTimeout(() => {
        if (isListening.value) {
          console.log('语音识别超时，自动停止')
          recognition.value.stop()
          isListening.value = false
          ElMessage.closeAll()
          ElMessage.info('语音识别超时，请重试')
        }
      }, 10000)

    } catch (error) {
      console.error('启动语音识别失败:', error)
      isListening.value = false
      ElMessage.closeAll()
      ElMessage.error('启动语音识别失败')
    }
  }
}
// 全局键盘处理
const handleGlobalKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    const activeElement = document.activeElement
    const isInputFocused = activeElement?.classList.contains('el-input__inner')
    const isButtonFocused = activeElement?.tagName === 'BUTTON'

    if ((isInputFocused || isButtonFocused) && !aiThinking.value && userInput.value.trim()) {
      e.preventDefault()
      sendMessage()
    }
  }
}
    // 监听用户信息更新
    window.addEventListener('user-updated', (event) => {
      const userData = event.detail
      if (userData.realName) {
        // 更新显示的用户名
        username.value = userData.realName || localStorage.getItem('username')
      }
    })
// 组件卸载时清理
onUnmounted(() => {
  if (recognition.value && isListening.value) {
    recognition.value.stop()
  }
  window.removeEventListener('keydown', handleGlobalKeyDown)
})
// 参数轮播
const paramList = ref([
  { name: 'GPS精度', value: '0.5', unit: 'm' },
  { name: '雷达回波', value: '95', unit: '%' },
  { name: '电阻', value: '120', unit: 'Ω' },
  { name: '压载量', value: '85', unit: '%' },
  { name: '分布均衡', value: '0.92', unit: '' }
])

// 主机参数
const engineParams = ref([
  { name: '油耗', standard: '70-90℃', current: '75℃', status: 'normal' },
  { name: '排烟温度', standard: '300-400℃', current: '360℃', status: 'normal' },
  { name: '滑油压力', standard: '0.2-0.4MPa', current: '0.3MPa', status: 'normal' },
  { name: '发动机电压', standard: '400V', current: '400V', status: 'normal' },
  { name: '发动机频率', standard: '50Hz', current: '50Hz', status: 'normal' }
])

// 航速建议
const currentSpeed = ref('11.0km/h')
const optimalSpeeds = ref([
  { value: '9.0km/h', label: '经济航速' },
  { value: '10.0km/h', label: '标准航速' },
  { value: '11.0km/h', label: '当前航速' }
])
const fuelAdvice = ref('建议保持当前航速，油耗处于经济区间')

// 仓库列表
const storageList = ref([
  { name: '货舱仓库', percentage: 85, color: '#67C23A' },
  { name: '冷库仓库', percentage: 62, color: '#409EFF' },
  { name: '集装箱仓库', percentage: 78, color: '#E6A23C' },
  { name: '冷冻仓库', percentage: 45, color: '#F56C6C' },
  { name: '散货仓库', percentage: 90, color: '#909399' }
])

// ==================== 工具函数 ====================
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getParamStatus = () => 'normal-value'

const getFaultLevelType = (level) => {
  const map = { 'high': 'danger', 'medium': 'warning', 'low': 'info' }
  return map[level] || 'info'
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesDiv.value) {
      chatMessagesDiv.value.scrollTop = chatMessagesDiv.value.scrollHeight
    }
  })
}

// ==================== 核心功能 ====================
// ==================== 语音播报功能 ====================

const speak = (text) => {
  console.log('尝试播报:', text)

  if (!('speechSynthesis' in window)) {
    console.error('浏览器不支持语音播报')
    ElMessage.warning('您的浏览器不支持语音播报')
    return
  }

  try {
    // 如果有正在播报的语音，先停止
    if (isSpeaking.value) {
      window.speechSynthesis.cancel()
    }

    const utterance = new SpeechSynthesisUtterance(text)
    currentUtterance = utterance
    utterance.lang = 'zh-CN'
    utterance.rate = 1.0
    utterance.pitch = 1.0
    utterance.volume = 1.0

    // 选择中文语音
    const voices = window.speechSynthesis.getVoices()
    const chineseVoice = voices.find(voice => voice.lang.includes('zh'))
    if (chineseVoice) {
      utterance.voice = chineseVoice
    }

    // 添加事件监听
    utterance.onstart = () => {
      console.log('开始播报')
      isSpeaking.value = true
    }

    utterance.onend = () => {
      console.log('播报结束')
      isSpeaking.value = false
      currentUtterance = null
    }

    utterance.onerror = (event) => {
      console.error('播报错误:', event)
      isSpeaking.value = false
      currentUtterance = null
    }

    window.speechSynthesis.speak(utterance)

  } catch (error) {
    console.error('语音播报失败:', error)
    isSpeaking.value = false
  }
}

// 停止语音播报
const stopSpeaking = () => {
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    currentUtterance = null
    ElMessage.info('语音播报已停止')
  }
}

// ==================== 发送消息（只保留这一个）====================
const sendMessage = async () => {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  const message = userInput.value
  console.log('发送消息:', message)

  chatMessages.value.push({
    type: 'user',
    content: message,
    time: new Date().toLocaleTimeString()
  })

  userInput.value = ''
  aiThinking.value = true
  scrollToBottom()

  try {
    const response = await shipApi.chat(1, message)
    console.log('AI回复完整响应:', response)

    if (response.data && response.data.code === 200) {
      const reply = response.data.data
      console.log('提取的回复内容:', reply)

      chatMessages.value.push({
        type: 'ai',
        content: reply,
        time: new Date().toLocaleTimeString()
      })

      // 自动语音播报AI回复
      console.log('准备调用语音播报...')
      // 先停止当前播报，再播报新的
      if (isSpeaking.value) {
        window.speechSynthesis.cancel()
      }
      speak(reply)
    } else {
      chatMessages.value.push({
        type: 'ai',
        content: response.data?.toString() || '无法获取回复',
        time: new Date().toLocaleTimeString()
      })
    }
  } catch (error) {
    console.error('发送失败:', error)
    chatMessages.value.push({
      type: 'ai',
      content: `请求失败: ${error.message}`,
      time: new Date().toLocaleTimeString()
    })
  } finally {
    aiThinking.value = false
    scrollToBottom()
  }
}

// 快捷提问
const quickQuestion = (question) => {
  userInput.value = question
  sendMessage()
}
// 加载故障列表
const loadFaults = async () => {
  try {
    const response = await shipApi.getFaults(1)
    if (response.data.code === 200) {
      faultList.value = response.data.data || []
      console.log('故障列表加载成功:', faultList.value.length)
    }
  } catch (error) {
    console.error('加载故障失败', error)
  }
}

// 触发多智能体分析
const triggerAnalysis = async () => {
  analyzing.value = true
  try {
    console.log('开始触发分析...')
    const response = await shipApi.analyze(1)
    console.log('分析响应:', response)

    if (response.data.code === 200) {
      ElMessage.success('分析完成')
      await loadFaults()
      ElMessage.success('故障列表已更新')
    } else {
      ElMessage.error('分析失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('分析错误:', error)
    ElMessage.error('分析失败: ' + (error.response?.data?.message || error.message))
  } finally {
    analyzing.value = false
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  // 1. 更新时间
  setInterval(() => {
    currentTime.value = new Date().toLocaleString()
  }, 1000)

  // 2. 加载故障
  loadFaults()

  // 3. 初始化图表
  nextTick(() => {
    const chartDom = document.querySelector('.chart-container')
    if (chartDom) {
      const chart = echarts.init(chartDom)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
        },
        yAxis: {
          type: 'value',
          name: '出勤人数'
        },
        series: [
          {
            name: '船员出勤',
            type: 'line',
            data: [120, 80, 150, 200, 180, 90],
            smooth: true,
            lineStyle: { color: '#409EFF', width: 3 },
            areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
          }
        ]
      })
    }
  })

  // 初始化语音识别
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    recognition.value = new SpeechRecognition()

    // 关键配置：只返回最终结果
    recognition.value.continuous = false      // 不连续识别
    recognition.value.interimResults = false  // 不返回中间结果
    recognition.value.lang = 'zh-CN'
    recognition.value.maxAlternatives = 1      // 只返回一个最佳结果

    // 只处理最终结果
    recognition.value.onresult = (event) => {
      // 确保是最终结果
      if (event.results[0].isFinal) {
        const transcript = event.results[0][0].transcript
        console.log('最终识别结果:', transcript)
        userInput.value = transcript

        if (autoSendAfterVoice.value) {
          // 延迟500ms发送，让用户看到结果
          setTimeout(() => {
            if (userInput.value.trim()) {
              sendMessage()
              ElMessage.success(`已发送: ${transcript}`)
            }
          }, 500)
        } else {
          ElMessage.info(`识别到: ${transcript}，按回车发送`)
        }
      }
    }

    // 语音结束
    recognition.value.onend = () => {
      console.log('语音识别结束 - 开始重置状态')
      console.log('重置前 isListening:', isListening.value)
      isListening.value = false
      console.log('重置后 isListening:', isListening.value)  // 确认是否真的变了
      ElMessage.closeAll()
    }

    // 错误处理
    recognition.value.onerror = (event) => {
      console.log('语音识别错误 - 开始重置状态')
      console.log('重置前 isListening:', isListening.value)
      isListening.value = false
      console.log('重置后 isListening:', isListening.value)
      if (event.error !== 'no-speech') {
        ElMessage.error('识别失败: ' + event.error)
      }
    }
  }

  // 绑定回车事件（新增）
  setTimeout(() => {
    const input = document.querySelector('.el-input__inner')
    if (input) {
      // 移除可能存在的旧事件
      input.removeEventListener('keyup', handleEnterKey)

      // 添加新事件
      input.addEventListener('keyup', handleEnterKey)
      console.log('回车事件绑定成功')
    } else {
      console.warn('未找到输入框元素')
    }
  }, 1000) // 延迟1秒确保DOM加载完成
})

// 回车事件处理函数（在 onMounted 外面定义）
const handleEnterKey = (e) => {
  if (e.key === 'Enter' && !aiThinking.value && userInput.value.trim()) {
    e.preventDefault()
    console.log('回车触发发送')
    sendMessage()
  }
}
</script>

<style scoped>
.dashboard {
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

.main-content {
  background: #f5f7fa;
  padding: 20px;
}

.chart-card, .param-card, .table-card, .speed-card, .fault-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.card-header {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-subtitle {
  font-size: 0.85rem;
  color: #909399;
  font-weight: normal;
  margin-left: 10px;
}

.chart-container {
  height: 200px;
  width: 100%;
}

.param-card {
  padding: 8px 16px;
}

.param-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.param-name {
  font-size: 1rem;
  color: #606266;
}

.param-value {
  font-size: 1.2rem;
  font-weight: bold;
  color: #2a5298;
}

.param-unit {
  font-size: 0.85rem;
  color: #909399;
  margin-left: 5px;
}

.speed-list {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
}

.speed-item {
  text-align: center;
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
  min-width: 100px;
}

.speed-item.active {
  background: #2a5298;
  color: white;
}

.speed-value {
  display: block;
  font-size: 1.2rem;
  font-weight: bold;
}

.speed-label {
  font-size: 0.85rem;
  opacity: 0.8;
}

.speed-advice {
  text-align: center;
  padding: 12px;
  background: #ecf5ff;
  border-radius: 8px;
  color: #2a5298;
}

.aside {
  background: white;
  padding: 20px;
  border-left: 1px solid #e4e7ed;
  width: 350px;
}

.ai-card, .storage-card {
  margin-bottom: 20px;
}

.chat-messages {
  height: 300px;
  overflow-y: auto;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 10px;
  max-width: 80%;
}

.message.user {
  margin-left: auto;
}

.message.ai {
  margin-right: auto;
}

.message-content {
  padding: 8px 12px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.message.user .message-content {
  background: #2a5298;
  color: white;
}

.message-time {
  font-size: 0.7rem;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.chat-input {
  margin-bottom: 10px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-actions .el-button {
  flex: 1 1 auto;
}

.storage-item {
  margin-bottom: 15px;
}

.storage-item span {
  display: block;
  margin-bottom: 5px;
  font-size: 0.9rem;
  color: #606266;
}

.normal-value {
  color: #67C23A;
  font-weight: 500;
}

.warning-value {
  color: #E6A23C;
  font-weight: 500;
}

.danger-value {
  color: #F56C6C;
  font-weight: 500;
}

.el-carousel__item {
  background: linear-gradient(90deg, #f5f7fa 0%, #ffffff 100%);
}

:deep(.el-carousel__indicators) {
  display: none;
}
/* 语音按钮动画 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}

.el-button.is-danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
  animation: pulse 1.5s infinite;
}

.voice-status {
  margin-top: 10px;
  text-align: center;
}

.voice-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.chat-input-wrapper {
  margin-bottom: 10px;
}

.auto-send-option {
  margin: 10px 0;
  text-align: right;
}
/* 停止按钮样式 */
.el-button.is-danger.is-circle {
  margin-left: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
</style>