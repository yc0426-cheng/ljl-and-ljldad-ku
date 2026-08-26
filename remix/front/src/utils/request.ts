/**
 * Axios 请求实例
 *
 * 结构参照成熟后台模板，已针对当前后端做了适配。包含：
 * - 基础 URL 配置
 * - 请求拦截器（添加 token；POST 请求体国密加密——后端暂未实现，见 utils/crypto.ts）
 * - 响应拦截器（按 HTTP 状态码统一错误处理，401/403/危险级 500 踢出登录）
 * - 类型化请求门面 request（get / post / getPage / postPage / download / preview / getBlobUrl）
 *
 * 与模板后端的差异（当前后端暂不具备，已做适配，注释中均有标注）：
 * 1. 无统一响应包装 {code, data, message}：登录接口成功直接返回纯字符串 token，
 *    因此拦截器不做 code 校验、request 门面直接取 res.data；
 *    等后端补全统一响应格式后可恢复 code === '200' 校验。
 * 2. 无国密解密：utils/crypto.ts 目前为透传占位，等后端实现后填真实加密。
 */
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/common/api'
import type { PageResult } from '@/types/common/page'
import { encryptFormData, encryptRequestData } from '@/utils/crypto'
import { logout } from '@/utils/auth'

// 创建 axios 实例
// baseURL：开发环境走 vite 代理（vite.config.ts 中 /auth → localhost:11000），故留空；
// 生产环境可改为 '/api' 等由 nginx 反向代理
// timeout：请求超时时间 10 秒
const service: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 10000
})

/**
 * 请求拦截器
 *
 * 在发送请求前：
 * 1. 添加认证 token 到请求头
 * 2. 对 POST 请求体进行国密加密（当前为透传占位，见 utils/crypto.ts）
 */
service.interceptors.request.use(
  async (config) => {
    // token 存于 localStorage（登录成功后由 store/user.ts 写入），刷新页面不丢失；
    // 注：模板用 sessionStorage，本项目保持 localStorage 以实现刷新后保持登录
    const token = localStorage.getItem('token')
    if (token) {
      // 标准 Bearer 方案，等后端网关实现鉴权后按实际约定调整
      config.headers.Authorization = `Bearer ${token}`
    }

    // POST 且有请求体时进行加密
    if (config.method?.toLowerCase() === 'post' && config.data) {
      try {
        // 判断是否为 FormData（文件上传等场景）
        if (config.data instanceof FormData) {
          config.data = await encryptFormData(config.data)
        } else {
          // 普通对象加密（登录的 URLSearchParams 等）
          config.data = await encryptRequestData(config.data)
        }
      } catch (error) {
        ElMessage.error('请求加密失败，请重试')
        return Promise.reject(error)
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 *
 * 处理逻辑：
 * - 2xx：直接透传 response（后端无统一包装），由 request 门面取 .data
 * - 400：客户端错误，显示 message
 * - 401：会话失效，弹框踢出登录
 * - 403：越权访问，弹框踢出登录
 * - 500：code 以 D 开头视为危险级异常（会话失效类）踢出登录，否则显示 message
 * - 其他 / 网络异常：显示对应提示
 */
service.interceptors.response.use(
  (response) => {
    // blob 响应（下载 / 预览）需要保留完整 response（含 headers 与 data），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 适配说明：模板结构在此校验 res.code === '200'，不通过则全局提示并 reject；
    // 当前后端没有统一响应包装（登录返回纯字符串 token），故直接透传整个 response，
    // 等后端补全 {code, data, message} 格式后在此恢复 code 校验。
    return response
  },
  async (error) => {
    if (error.response) {
      const status = error.response.status
      const raw = error.response.data
      let data: ApiResponse | undefined
      // 请求级跳过全局提示（调用方通过 config.skipGlobalError 指定）
      const skipMsg = (error.config as Record<string, unknown> | undefined)?.skipGlobalError as
        boolean | undefined

      // blob 错误体尝试解析为 JSON（后端异常包装）
      if (raw instanceof Blob) {
        try {
          data = JSON.parse(await raw.text())
        } catch {
          /* 非 JSON 格式 blob，保持 undefined */
        }
      } else {
        data = raw as ApiResponse
      }

      // 按状态码确定提示信息与是否踢出登录
      let message = '请求失败'
      let kickOut = false
      switch (status) {
        case 400:
          message = data?.message || '请求参数错误'
          break
        case 401:
          // 会话失效：弹确认框，确认后清除登录态并跳回登录页
          message = data?.message || '登录已失效'
          kickOut = true
          break
        case 403:
          // 越权：与 401 同样处理
          message = data?.message || '越权访问'
          kickOut = true
          break
        case 500:
          if (data?.code?.startsWith('D')) {
            // 危险级系统异常（会话失效类）：踢出登录
            message = data?.message || '登录已失效'
            kickOut = true
          } else {
            message = data?.message || '服务器错误'
          }
          break
        default:
          message = data?.message || `请求失败（状态码 ${status}）`
      }

      if (kickOut) {
        // 踢出登录：弹框确认后清理并跳转（utils/auth.ts）
        logout(message, { needConfirm: true, closeOtherTabs: true })
      } else if (!skipMsg) {
        // 普通错误：全局提示（调用方可传 skipGlobalError 跳过）
        ElMessage.error(message)
      }

      // 包装为带可读 message 的 Error，方便调用方 catch 后直接展示 e.message
      return Promise.reject(new Error(message))
    }

    // 网络层异常：请求已发出但未收到响应（后端未启动、网络断开、跨域被拦等）
    const message = '无法连接服务器，请检查后端是否已启动'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }
)

/**
 * 封装通用请求方法（类型化 request 门面）
 *
 * 提供类型安全的请求函数；当前后端无统一包装，因此 get/post 直接返回 res.data。
 *
 * @example
 * ```typescript
 * // GET 请求（带查询参数）
 * return request.get<UserInfo>('/user/info', { params: { id: 123 } })
 *
 * // POST 请求（登录：表单格式，后端无 @RequestBody 时用 URLSearchParams）
 * return request.post<string>('/auth/login', params)
 *
 * // 文件下载
 * await request.download('/file/download?fileId=xxx')
 *
 * // 文件预览（新标签页打开）
 * await request.preview('/file/preview?fileId=xxx')
 *
 * // 文件上传（FormData 无需手动设 Content-Type，拦截器自动识别跳过对象加密）
 * const fd = new FormData()
 * fd.append('file', file)
 * await request.post('/file/upload', fd)
 * ```
 */
export const request = {
  /**
   * GET 请求，返回后端数据（无统一包装时即 res.data）
   */
  async get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const res = await service.get(url, config)
    return res.data as T
  },

  /**
   * GET 分页查询
   * 依赖后端统一分页包装（MyBatis-Plus IPage：records / total），
   * 当前后端暂无分页接口，待后端提供后使用。
   */
  async getPage<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<PageResult<T>> {
    const res = await service.get(url, config)
    return (res.data as ApiResponse<PageResult<T>>).data
  },

  /**
   * POST 请求，返回后端数据（登录接口即返回 token 字符串）
   */
  async post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const res = await service.post(url, data, config)
    return res.data as T
  },

  /**
   * POST 分页查询（依赖后端统一分页包装，待后端提供后使用）
   */
  async postPage<T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<PageResult<T>> {
    const res = await service.post(url, data, config)
    return (res.data as ApiResponse<PageResult<T>>).data
  },

  /**
   * 下载文件并触发浏览器保存对话框
   *
   * @param url - 请求地址
   * @param fileName - 下载文件名，不传则从 Content-Disposition 头解析
   * @param method - 请求方式，默认 GET
   * @param data - 请求体（POST 时自动加密）
   */
  async download(
    url: string,
    fileName?: string,
    method: 'GET' | 'POST' = 'GET',
    data?: unknown
  ): Promise<void> {
    const config: AxiosRequestConfig = { responseType: 'blob' }
    const res = await (method === 'GET'
      ? service.get(url, config)
      : service.post(url, data, config))

    // 优先用传入的文件名，其次解析 Content-Disposition 头，最后兜底 'download'
    const disposition = (res.headers as Record<string, string>)?.['content-disposition']
    const headerName = disposition?.split('filename=')?.[1]?.replace(/['"]/g, '')
    const name = fileName || (headerName && decodeURIComponent(headerName)) || 'download'

    // 创建临时链接触发浏览器下载
    const blobUrl = window.URL.createObjectURL(res.data as Blob)
    const a = document.createElement('a')
    a.href = blobUrl
    a.download = name
    a.click()
    window.URL.revokeObjectURL(blobUrl)
  },

  /**
   * 在新标签页中预览文件（不触发下载）
   *
   * @param url - 请求地址
   * @param method - 请求方式，默认 GET
   * @param data - 请求体（POST 时自动加密）
   * @param mimeType - Blob MIME 类型，默认 PDF
   */
  async preview(
    url: string,
    method: 'GET' | 'POST' = 'GET',
    data?: unknown,
    mimeType = 'application/pdf'
  ): Promise<void> {
    const config: AxiosRequestConfig = { responseType: 'blob' }
    const res = await (method === 'GET'
      ? service.get(url, config)
      : service.post(url, data, config))
    const blobUrl = window.URL.createObjectURL(new Blob([res.data as BlobPart], { type: mimeType }))
    window.open(blobUrl)
  },

  /**
   * 获取文件 blob URL（用于图片 / PDF 内嵌展示，配合 <img> 或 <iframe> 使用）
   *
   * @param url - 请求地址
   * @param params - 查询参数
   * @returns blob URL（用完记得 URL.revokeObjectURL 释放）
   */
  async getBlobUrl(url: string, params?: Record<string, string>): Promise<string> {
    const res = await service.get(url, { params, responseType: 'blob' })
    return window.URL.createObjectURL(res.data as Blob)
  }
}

export default service
