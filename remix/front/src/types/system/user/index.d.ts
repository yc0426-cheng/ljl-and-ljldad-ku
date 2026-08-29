import {BaseEntity} from "@/types/common/base";

/**
 * 系统用户类
 * - 对应system模块 SysUser 实体类
 */
export interface SysUser extends BaseEntity {
    /** 用户id */
    userId: string;
    /** 账号 */
    account?: string;
    /** 姓名 */
    name?: string;
    /** 密码 */
    password?: string;
    /** 手机号 */
    phone?: string;
    /** 身份证号 */
    idNumber?: string;
    /** 邮箱 */
    email?: string;
    /** 登录错误次数 */
    passErrorCount: number;
    /** 账号状态 */
    status: string;
    /** 最后登录时间 */
    lastLoginTime?: Date;
    /** 删除标记 */
    delFlag?: boolean;
}