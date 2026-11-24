import { request } from '@/utils/request'

/**
 *  登录Api
 */
export class LoginApi {
  /**
   * 根据账号获取登录失败次数
   *
   * @param account 账号
   * @return 失败次数
   */
  static getErrorCount(account: string): Promise<number> {
    return request.post<number>('/auth/error-count', undefined, {
      params: { account },
      skipGlobalError: true
    })
  }
}
