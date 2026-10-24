import { BookOpenVO, BookPageVO } from '@/types/book'
import { request } from '@/utils/request'

/**
 * 书籍分页Api
 */
export class BookPageApi {
  /**
   * 获取页码数据
   * 注：后端 BookPageController#getPageBook 无 @RequestBody，参数走 query 绑定
   *
   * @param bookId 书id
   * @return 书籍分页结果
   */
  static get(bookId: number): Promise<BookPageVO[]> {
    return request.post('/book/page/get', undefined, { params: { bookId } })
  }

  /**
   * 翻开书籍
   *
   * @param bookId 书籍id
   * @return 书籍信息 + 首页内容 + 总页数
   */
  static open(bookId: number): Promise<BookOpenVO> {
    return request.get(`/book/page/${bookId}/open`)
  }

  /**
   * 翻页
   *
   * @param bookId 书籍id
   * @param pageNo 页码
   * @return 页数据
   */
  static readPage(bookId: number, pageNo: number): Promise<BookPageVO> {
    return request.get(`/book/page/${bookId}/pages/${pageNo}`)
  }

  /**
   * 定位
   *
   * @param bookId     书籍id
   * @param charOffset 字符偏移量
   * @return 定位数据
   */
  static locate(bookId: number, charOffset: number): Promise<BookPageVO> {
    return request.get(`/book/page/${bookId}/locate`, { params: { charOffset } })
  }
}
