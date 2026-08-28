/**
 * 统一 API 响应格式
 *
 * 后端暂未提供统一包装（当前登录接口直接返回纯字符串 token），
 * 此类型用于【后端将来补全统一响应格式后】的对接：
 * request.ts 恢复 code 校验、getPage/postPage 解包 data 时使用。
 *
 * @template T - 响应数据的实际类型
 * @property code - 状态码，'200' 表示成功
 * @property data - 响应数据
 * @property message - 提示信息
 */
export interface ApiResponse<T = unknown> {
  code: string
  data: T
  message: string
}
