import { UploadAvatarResult } from '@/types/system/user'
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
    return request.post('/api/user/uploadAvatar', data)
  }
}
