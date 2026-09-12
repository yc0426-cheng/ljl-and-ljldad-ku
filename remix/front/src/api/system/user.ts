import { EditUserInfoForm, UploadAvatarResult } from '@/types/system/user'
import { request } from '@/utils/request' // 项目封装的 axios 实例，按实际路径调整

/**
 * 上传文件API
 */
export class uploadApi {
  /**
   * 上传头像
   * 链路：前端仅提交文件本体 → 后端保存至阿里云 OSS 并写库 → 返回 URL
   */
  static uploadingAvatar(data: FormData): Promise<UploadAvatarResult> {
    return request.post('/user/uploadAvatar', data)
  }
}

/**
 * 用户信息API
 */
export class userApi {
  /**
   * 修改用户信息
   * 链路：前端提交 JSON（name/phone/email 写 sys_user，nickname/signature 写 sys_user_misc）
   * 待后端实现：system 模块 SysUserController 新增编辑接口后即可联通；
   * 失败提示由 request 拦截器全局弹出，调用方无需重复提示
   */
  static editUserInfo(data: EditUserInfoForm): Promise<void> {
    return request.post('/user/edit', data)
  }
}
