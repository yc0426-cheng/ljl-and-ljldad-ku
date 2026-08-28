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
