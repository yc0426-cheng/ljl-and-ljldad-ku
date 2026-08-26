/**
 * 国密加密工具（占位实现）
 *
 * 请求结构要求 POST 请求体做 SM2/SM3/SM4 国密加密（见 types/common/api.ts 的 ApiRequest），
 * 但当前后端【尚未实现国密解密】，因此这两个函数先做"原样透传"，
 * 保证 request.ts 拦截器里的加密调用路径完整可用。
 *
 * 等后端实现国密后，在此填入真实加密逻辑即可，函数签名保持不变：
 * - encryptRequestData：普通对象 / URLSearchParams → 加密后请求体
 * - encryptFormData：FormData 中普通字段加密后重新构造
 */

/**
 * 加密普通请求体
 * @param data 原始请求体（对象 / URLSearchParams 等）
 * @returns 加密后的请求体
 */
export async function encryptRequestData(data: unknown): Promise<unknown> {
  // TODO 国密加密：后端支持后按 ApiRequest 结构返回（sm4Data / sm4Key / sm4Iv / sm3Data / nonce）
  return data
}

/**
 * 加密 FormData 请求体
 * @param data 原始 FormData
 * @returns 加密后的 FormData
 */
export async function encryptFormData(data: FormData): Promise<FormData> {
  // TODO 国密加密：遍历字段加密后重新构造 FormData
  return data
}
