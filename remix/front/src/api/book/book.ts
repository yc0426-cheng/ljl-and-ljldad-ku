import { BookDTO, BookParam, BookVO } from '@/types/book'
import { request } from '@/utils/request'

/**
 * 书籍Api
 * 注：后端 BookController 各方法入参均无 @RequestBody，参数由 query 绑定，
 * 故本类均以 query 传参；后端补 @RequestBody 后可改为 JSON body
 */
export class BookApi {
  /**
   * 查看书籍信息
   *
   * @param id 书籍id
   * @return 书籍返回结果类
   */
  static list(id: number): Promise<BookVO> {
    return request.post('/book/list', undefined, { params: { id } })
  }

  /**
   * 新增书籍(设计书籍)
   *
   * @param dto 新增书籍数据
   */
  static add(dto: BookDTO): Promise<void> {
    return request.post('/book/add', undefined, { params: { ...dto } })
  }

  /**
   * 删除书籍
   *
   * @param id 待删除书籍id
   */
  static delete(id: number): Promise<void> {
    return request.post('/book/delete', undefined, { params: { id } })
  }

  /**
   * 修改书籍(设计书籍)
   * ⚠️ 后端 BookDTO 无 id 字段，后端无法定位被修改书籍（后端问题），此处按原样透传
   *
   * @param dto 修改书籍数据
   */
  static edit(dto: BookDTO): Promise<void> {
    return request.post('/book/edit', undefined, { params: { ...dto } })
  }

  /**
   * 上传书籍
   *
   * @param file   书籍文件
   * @param userId 用户id
   */
  static uploadBook(file: File, userId: number): Promise<void> {
    const data = new FormData()
    data.append('file', file)
    data.append('userId', String(userId))
    return request.post('/book/upload', data)
  }

  /**
   * 导出书籍
   * 注：request.download 不支持 query 参数，BookParam 需自行拼到 URL 上
   *
   * @param param 待导出数据
   */
  static exportBook(param: BookParam): Promise<void> {
    const query = new URLSearchParams()
    if (param.id != null) {
      query.set('id', String(param.id))
    }
    if (param.title) {
      query.set('title', param.title)
    }
    if (param.uploaderId != null) {
      query.set('uploaderId', String(param.uploaderId))
    }
    const qs = query.toString()
    return request.download(`/book/export${qs ? `?${qs}` : ''}`, '书籍信息表.xlsx', 'POST')
  }
}
