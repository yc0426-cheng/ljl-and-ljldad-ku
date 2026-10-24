import { FileApi } from '@/api/file/file'
import { EditUserInfoForm, UploadAvatarResult } from '@/types/system/user'
import { request } from '@/utils/request'

/**
 * 头像上传Api（用户设置页用）
 * 真实后端链路：POST /file/avatar/upload 转存 OSS（无返回值）→ GET /file/get/avatar 取最新地址
 */
export class UploadApi {
  /**
   * 上传头像
   * 链路：前端仅提交文件本体 → 后端保存至阿里云 OSS → 再查询返回 URL
   *
   * @param data 表单数据（file：文件；userId：用户id）
   * @return 头像访问地址
   */
  static async uploadingAvatar(data: FormData): Promise<UploadAvatarResult> {
    await FileApi.uploadingAvatar(data)
    const userId = Number(data.get('userId'))
    const url = await FileApi.getAvatar(userId)
    return { url }
  }
}

/**
 * 用户信息Api（用户设置页用）
 */
export class UserApi {
  /**
   * 修改用户信息
   * 后端 SysUserController#edit 入参无 @RequestBody，参数由 query 绑定；
   * 仅 name/phone/email 写 sys_user（nickname/signature 属 sys_user_misc，
   * 后端暂无编辑接口，后端补充后在此扩展）；
   * 失败提示由 request 拦截器全局弹出，调用方无需重复提示
   *
   * @param data   编辑表单
   * @param userId 用户id
   */
  static editUserInfo(data: EditUserInfoForm, userId: number): Promise<void> {
    return request.post('/sys/user/edit', undefined, {
      params: {
        userId,
        name: data.name,
        phone: data.phone,
        email: data.email
      }
    })
  }
}
