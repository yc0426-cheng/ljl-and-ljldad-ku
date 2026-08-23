import request from '@/utils/request' // 导入封装好的 axios 实例

/**
 * 用户登录
 * 对应后端：auth 模块 LoginController#login
 *   POST /auth/login
 *   入参：account、password
 *   返回：token 字符串（成功后由调用方保存）
 *
 * 注意：后端 LoginDTO 没有加 @RequestBody，Spring 是按【表单参数】绑定的，
 * 所以前端必须用 URLSearchParams 以 application/x-www-form-urlencoded 格式提交，
 * 直接传 JSON 对象后端会收不到参数。
 */
export function login(account: string, password: string): Promise<string> {
  // 构造表单参数
  const params = new URLSearchParams()
  params.append('account', account)
  params.append('password', password)
  // 响应拦截器已把 response 解包为 data，登录接口的 data 即 token 字符串，故断言类型为 Promise<string>
  return request.post('/auth/login', params) as Promise<string>
}

/**
 * 用户登出
 * 对应后端：LoginController#logout
 * 注意：后端该方法目前【没有 @PostMapping 注解】，接口尚未真正暴露，
 * 前端先留好调用位置，等后端补全注解后即可直接使用。
 */
export function logout(): Promise<unknown> {
  return request.post('/auth/logout')
}
