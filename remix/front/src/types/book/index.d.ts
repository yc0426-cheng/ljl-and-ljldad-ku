/**
 * 书籍服务-书籍返回结果类
 */
export interface BookVO {
  /** ID，全局唯一 */
  id: number
  /** 书名 */
  title: string
  /** 封面图OSS地址 */
  coverUrl?: string
  /** 书籍原文件OSS地址 */
  fileUrl?: string
  /** 文件格式 */
  fileFormat?: string
  /** 文件大小(字节) */
  fileSize?: number
  /** 文件SHA-256 */
  fileHash?: string
  /** 总页数 */
  totalPages?: number
  /** 书籍语言 */
  language?: string
  /** 状态：1正常 0下架 2解析中 3解析失败 */
  status?: number
  /** 上传者ID */
  uploaderId?: number
}

/**
 * 书籍服务-增删改dto类
 */
export interface BookDTO {
  /** 书名 */
  title?: string
  /** 封面图OSS地址 */
  coverUrl?: string
  /** 书籍原文件OSS地址 */
  fileUrl?: string
  /** 文件格式 */
  fileFormat?: string
  /** 文件大小(字节) */
  fileSize?: number
  /** 文件SHA-256 */
  fileHash?: string
  /** 总页数 */
  totalPages?: number
  /** 书籍语言 */
  language?: string
  /** 上传者ID */
  uploaderId?: number
}

/**
 * 书籍服务-查询类
 */
export interface BookParam {
  /** id */
  id?: number
  /** 书名 */
  title?: string
  /** 上传者 */
  uploaderId?: number
}

/**
 * 书籍服务-书籍分页返回结果类
 */
export interface BookPageVO {
  /** 分页记录主键ID */
  bookPageId: number
  /** 书籍ID */
  bookId: number
  /** 页码 */
  pageNo: number
  /** 该页渲染资源OSS地址 */
  contentUrl?: string
  /** 该页纯文本内容 */
  contentText?: string
  /** 该页文本在全书中的起始字符偏移 */
  charStart?: number
  /** 该页文本在全书中的结束字符偏移 */
  charEnd?: number
}

/**
 * 书籍服务-首页信息返回结果类
 */
export interface BookOpenVO {
  /** 书籍元信息 */
  book: BookVO
  /** 总页数 */
  totalPages: number
  /** 默认首页内容（后续页按 pageNo 懒加载）；字段名沿用后端 BookOpenVO.pageNo */
  pageNo: BookPageVO
}
