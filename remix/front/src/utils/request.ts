import axios from 'axios' // HTTP 客户端：负责与后端交互

// 创建 axios 实例，统一管理请求配置
const request = axios.create({
  // 后端服务地址：
  // 1. 开发环境：留空，走 vite 代理（见 vite.config.ts 的 server.proxy，/auth → 11000），无跨域问题
  // 2. 若不想用代理，可直接填后端地址，例如 'http://localhost:11000'
  baseURL: '',
  // 请求超时时间（毫秒）
  timeout: 10000
})

// ---------------- 请求拦截器：每次请求发出前统一处理 ----------------
request.interceptors.request.use(
  (config) => {
    // 登录成功后 token 会被写入 localStorage（见 store/user.ts），这里取出来
    const token = localStorage.getItem('token')
    // 有 token 就放进请求头，供后端/网关校验登录态
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error) // 请求配置出错，直接抛给调用方
)

// ---------------- 响应拦截器：统一处理响应数据与错误 ----------------
request.interceptors.response.use(
  // 2xx 成功回调
  (response) => {
    // 直接返回数据部分（response.data）。
    // 注意：后端登录接口成功时返回的是【纯字符串 token】，不是 {code, data} 结构，
    // 因此这里不做解构，调用方拿到的 res 就是 token 本身。
    return response.data
  },
  // 非 2xx 失败回调
  (error) => {
    let message = '网络异常，请稍后重试' // 兜底错误信息

    if (error.response) {
      // 情况一：后端有响应（返回了 4xx/5xx 状态码）
      // 目前后端还没有全局异常处理器，业务异常(BizException)会变成 Spring 默认错误页，
      // 可取到 error 字段（如 "Internal Server Error"）；
      // 等后端补全全局异常处理后，可优先读取 error.response.data.message
      message =
        error.response.data?.message ||
        error.response.data?.error ||
        `请求失败（状态码 ${error.response.status}）`

      // 401 未登录 / 登录过期：清除本地 token 并跳回登录页
      if (error.response.status === 401) {
        localStorage.removeItem('token')
        // 防止在登录页自身报 401 时造成循环跳转
        if (!location.pathname.startsWith('/login')) {
          location.href = '/login'
        }
      }
    } else if (error.request) {
      // 情况二：请求已发出但没收到响应（后端未启动、网络断开、跨域被拦等）
      message = '无法连接服务器，请检查后端是否已启动'
    }
    // 统一包装成 Error 对象抛出，调用方通过 e.message 拿到可读信息
    return Promise.reject(new Error(message))
  }
)

export default request // 导出封装好的实例，供 api 模块使用
