import { request } from '@/utils/request'

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
  /** 手机号码 */
  phone?: string
  /** 邮箱 */
  email?: string
  /** 昵称 */
  nickname?: string
  /** 个性签名 */
  signature?: string
  /** token */
  token?: string
  /** 头像（OSS 地址，存于 sys_user_misc 杂项表，后端拼进 LoginUserInfo 返回） */
  avatar?: string
}

/**
 * 认证中心-登录相关Api
 */
export class AuthApi {
  /**
   * 用户登录
   *
   * @param account  账号
   * @param password 密码
   * @return token
   */
  static login(account: string, password: string): Promise<string> {
    // 后端 LoginDTO 带 @RequestBody，须以 JSON 提交；
    // skipGlobalError：登录失败的错误由登录页自行展示，避免与全局提示重复
    return request.post<string>('/auth/login', { account, password }, { skipGlobalError: true })
  }

  /**
   * 根据账号查询登录密码错误次数
   *
   * @param account 账号
   * @return 密码错误次数；账号不存在时返回 0
   */
  static getErrorCount(account: string): Promise<number> {
    // 后端为 @RequestParam，参数走 query；skipGlobalError：失败次数由登录页自行展示
    return request.post<number>('/auth/error-count', undefined, {
      params: { account },
      skipGlobalError: true
    })
  }

  /**
   * 校验token是否有效
   * 请求头 Authorization: Bearer <token> 由 request.ts 请求拦截器自动携带，无需手动传
   *
   * @return token对应的用户信息；token无效时后端抛业务异常 → 走 catch
   */
  static checkToken(): Promise<AuthUserInfo> {
    // skipGlobalError：校验失败由路由守卫自行处理（清 token 跳登录页），不弹全局提示
    return request.post<AuthUserInfo>('/auth/check', undefined, { skipGlobalError: true })
  }

  /**
   * 登出
   * 后端从请求头 Authorization 剥离 Bearer 拿纯 token 删 redis 并加黑名单；
   * 调用失败也无需前端兜底（store 内已 try/catch），本地清理照常执行
   */
  static logout(): Promise<void> {
    return request.post('/auth/logout', undefined, { skipGlobalError: true })
  }
}
