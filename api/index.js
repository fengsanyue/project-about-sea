import axios from 'axios'

// 创建axios实例，增加超时时间到30秒
const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 30000  // 从10000改为30000（30秒）
})

// 添加错误拦截器
api.interceptors.response.use(
    response => response,
    error => {
        console.error('API错误详细:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status,
            config: error.config
        })
        return Promise.reject(error)
    }
)

// 船舶相关接口
export const shipApi = {
    test() {
        return api.get('/ship/test')
    },
    getStatus(shipId) {
        return api.get(`/ship/status/${shipId}`)
    },
    analyze(shipId) {
        return api.post(`/ship/analyze/${shipId}`)
    },
    chat(shipId, message) {
        return api.post('/ship/chat', { shipId, message }, {
            timeout: 30000  // 单独设置聊天接口的超时时间
        })
    },
    getSensorData(shipId) {
        return api.get(`/ship/sensor-data/${shipId}`)
    },
    getFaults(shipId) {
        return api.get(`/ship/faults/${shipId}`)
    }
}

export default api