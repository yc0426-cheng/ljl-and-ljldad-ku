import 'axios'

/**
 * 扩展 axios 请求配置
 *
 * skipGlobalError：单个请求跳过响应拦截器里的全局错误提示，
 * 适用于"错误由调用方自行展示"的场景（如登录页自行展示失败原因）。
 */
declare module 'axios' {
  export interface AxiosRequestConfig {
    /** 跳过全局错误提示（调用方自行处理错误时传 true） */
    skipGlobalError?: boolean
  }
}
