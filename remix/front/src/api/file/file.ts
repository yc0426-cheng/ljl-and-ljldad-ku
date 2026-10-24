import { request } from '@/utils/request'

/**
 * 文件Api
 * 注：后端 FileController#downloadOssToStream 入参为 OutputStream，
 * 属服务内部方法误暴露为端点，前端无法调用，未封装
 */
export class FileApi {
  /**
   * 头像上传
   * 后端转存 OSS；该接口无返回值，地址需再调 getAvatar 获取
   *
   * @param data 表单数据（file：文件；userId：用户id）
   */
  static uploadingAvatar(data: FormData): Promise<void> {
    return request.post('/file/avatar/upload', data)
  }

  /**
   * 获取头像
   *
   * @param userId 用户id
   * @return 头像地址
   */
  static getAvatar(userId: number): Promise<string> {
    return request.get('/file/get/avatar', { params: { userId } })
  }

  /**
   * 获取历史头像
   *
   * @param userId 用户id
   * @return 历史头像oss地址列表
   */
  static getHistoryAvatars(userId: number): Promise<string[]> {
    return request.get('/file/get/history/avatars', { params: { userId } })
  }

  /**
   * 书籍上传
   *
   * @param file   书籍文件
   * @param userId 用户id
   */
  static uploadBook(file: File, userId: number): Promise<void> {
    const data = new FormData()
    data.append('file', file)
    data.append('userId', String(userId))
    return request.post('/file/book/upload', data)
  }
}
