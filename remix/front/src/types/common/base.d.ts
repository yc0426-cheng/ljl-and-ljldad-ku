/**
 * 基础实体类型
 *
 * 所有数据库实体模型的基类，包含审计字段。
 *
 * @property createUser - 创建用户ID
 * @property createTime - 创建时间
 * @property updateUser - 更新用户ID
 * @property updateTime - 更新时间
 */
export interface BaseEntity {
    createUser?: string
    createTime?: string
    updateUser?: string
    updateTime?: string
}

/**
 * 基础分页参数
 *
 * 用于分页查询的通用参数。
 *
 * @property pageSize - 每页显示数量
 * @property currentPage - 当前页码
 */
export interface BaseParam {
    pageSize?: number
    currentPage?: number
}