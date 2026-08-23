/**
 * 统一分页结果结构
 *
 * 与后端 MyBatis-Plus 的 IPage 序列化结构对应（records / total / size / current / pages）。
 * 当前后端暂无分页接口，待后端提供后由 request.getPage / postPage 使用。
 *
 * @template T - 单条记录类型
 */
export interface PageResult<T = unknown> {
  /** 当前页记录列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 每页条数 */
  size?: number
  /** 当前页码 */
  current?: number
  /** 总页数 */
  pages?: number
}
