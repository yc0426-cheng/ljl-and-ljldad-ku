import { request } from '@/utils/request' // 类型化请求门面（request.post<T> 直接返回后端数据）

/**
 * 后端 /auth/check 返回的用户信息结构（对应后端 LoginUserInfo）
 * userId / account / name 必有（后端封装），token 仅在部分场景有值
 */
export interface AuthUserInfo {
  /** 用户 id */
  userId?: number
  /** 账号 */
  account?: string
  /** 名称 */
  name?: string
  /** token */
  token?: string
  /** 头像（OSS 地址，存于 sys_user_misc 杂项表，后端拼进 LoginUserInfo 返回） */
  avatar?: string
}

/**
 * 用户登录
 * 对应后端：auth 模块 LoginController#login
 *   POST /auth/login
 *   入参：account、password
 *   返回：token 字符串
 *
 * 注意：后端 LoginDTO 加 @RequestBody
 * 所以前端必须用 URLSearchParams 以 application/x-www-form-urlencoded 格式提交，
 * 直接传 JSON 对象后端会收不到参数。
 */
export function login(account: string, password: string): Promise<string> {
  // skipGlobalError：登录失败的错误由登录页自行展示，避免与全局提示重复
  return request.post<string>(
    '/auth/login',
    {
      account,
      password
    },
    { skipGlobalError: true }
  )
}

/**
 * 用户登出
 * 对应后端：LoginController#logout
 *   POST /auth/logout
 * 后端会删除 redis 中的 token 并加入黑名单；
 * 调用失败也无需前端兜底（store 内已 try/catch），本地清理照常执行。
 */
export function logout(): Promise<unknown> {
  return request.post('/auth/logout', undefined, { skipGlobalError: true })
}

/**
 * 校验token是否有效
 * 对应后端：LoginController#check
 *   POST /auth/check
 *   请求头：Authorization: Bearer <token>（request.ts 请求拦截器自动携带，无需手动传）
 *   返回：token 对应的用户信息（LoginUserInfo）；token 无效时后端抛业务异常 → 走 catch
 *
 * 用途：路由守卫在页面加载/跳转时校验 localStorage 里的旧 token，
 * 并把返回的用户信息同步回 store（刷新后主页/个人设置页有真实数据显示）。
 */
export function checkToken(): Promise<AuthUserInfo> {
  // skipGlobalError：校验失败由守卫自行处理（清 token 跳登录页），不弹全局提示
  return request.post<AuthUserInfo>('/auth/check', undefined, { skipGlobalError: true })
}
