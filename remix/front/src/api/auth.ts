import { request } from '@/utils/request' // 类型化请求门面（request.post<T> 直接返回后端数据）

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
  return request.post<string>('/api/auth/login', {
    account,
    password
  }, { skipGlobalError: true })
}

/**
 * 用户登出
 * 对应后端：LoginController#logout
 * 注意：后端该方法目前【没有 @PostMapping 注解】，接口尚未真正暴露，
 * 调用会返回 404，这里先留好调用位置并跳过全局错误提示。
 */
export function logout(): Promise<unknown> {
  return request.post('/api/auth/logout', undefined, { skipGlobalError: true })
}

/**
 * 校验token是否有效
 * 对应后端：LoginController#check
 *   POST /auth/check
 *   请求头：Authorization: Bearer <token>（request.ts 请求拦截器自动携带，无需手动传）
 *   返回：token 对应的用户信息；token 无效时后端抛业务异常 → 走 catch
 *
 * 用途：路由守卫在页面加载/跳转时校验 localStorage 里的旧 token，
 * 避免后端 token 已过期但前端仍有残留 token 导致的"假登录态"。
 */
export function checkToken(): Promise<unknown> {
  // skipGlobalError：校验失败由守卫自行处理（清 token 跳登录页），不弹全局提示
  return request.post('/api/auth/check', undefined, { skipGlobalError: true })
}

/**
 * 校验token是否有效
 * 对应后端：LoginController#check
 *   POST /auth/check
 *   请求头：Authorization: Bearer <token>（request.ts 请求拦截器自动携带，无需手动传）
 *   返回：token 对应的用户信息；token 无效时后端抛业务异常 → 走 catch
 *
 * 用途：路由守卫在页面加载/跳转时校验 localStorage 里的旧 token，
 * 避免后端 token 已过期但前端仍有残留 token 导致的"假登录态"。
 */
export function checkToken(): Promise<unknown> {
  // skipGlobalError：校验失败由守卫自行处理（清 token 跳登录页），不弹全局提示
  return request.post('/api/auth/check', undefined, { skipGlobalError: true })
}