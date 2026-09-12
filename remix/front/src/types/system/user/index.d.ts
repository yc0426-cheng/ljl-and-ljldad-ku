import { BaseEntity } from '@/types/common/base'
import { SysUserStatusEnum } from './constants'

/**
 * 系统用户类
 * - 对应system模块 SysUser 实体类
 */
export interface SysUser extends BaseEntity {
  /** 用户id */
  userId?: string
  /** 账号 */
  account?: string
  /** 姓名 */
  name?: string
  /** 密码 */
  password?: string
  /** 手机号 */
  phone?: string
  /** 身份证号 */
  idNumber?: string
  /** 邮箱 */
  email?: string
  /** 登录错误次数 */
  passErrorCount?: number
  /** 账号状态 */
  status?: SysUserStatusEnum
  /** 最后登录时间 */
  lastLoginTime?: Date
  /** 删除标记 */
  delFlag?: boolean
}

/**
 * 系统服务-用户查询类
 */
export interface SysUserParam {
  account?: string
  name?: string
}

/** 头像上传结果：后端转存 OSS 后返回的公网访问地址 */
export interface UploadAvatarResult {
  url: string
}

/**
 * 修改用户信息表单
 * - name / phone / email：写 sys_user 主表
 * - nickname / signature：写 sys_user_misc 杂项表
 * 后端接口待实现（计划：system 模块新增 POST 编辑接口）
 */
export interface EditUserInfoForm {
  /** 姓名 */
  name: string
  /** 手机号码 */
  phone: string
  /** 邮箱 */
  email: string
  /** 昵称（sys_user_misc，可空） */
  nickname?: string
  /** 个性签名（sys_user_misc，可空） */
  signature?: string
}

/**
 * 系统服务-系统用户返回结果类
 */
export interface SysUserVO {
  /** 用户id */
  userId: string
  /** 账号 */
  account?: string
  /** 姓名 */
  name?: string
  /** 密码 */
  password?: string
  /** 手机号 */
  phone?: string
  /** 身份证号 */
  idNumber?: string
  /** 邮箱 */
  email?: string
  /** 登录错误次数 */
  passErrorCount: number
  /** 账号状态 */
  status: SysUserStatusEnum
  /** 最后登录时间 */
  lastLoginTime?: Date
  /** 删除标记 */
  delFlag?: boolean
}

/**
 * 系统服务-系统用户增删改类
 */
export interface SysUserDTO {
  /** 用户id */
  userId: string
  /** 账号 */
  account?: string
  /** 姓名 */
  name?: string
  /** 密码 */
  password?: string
  /** 手机号 */
  phone?: string
  /** 身份证号 */
  idNumber?: string
  /** 邮箱 */
  email?: string
  /** 账号状态 */
  status: SysUserStatusEnum
}
