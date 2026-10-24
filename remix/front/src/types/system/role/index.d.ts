import { BaseEntity } from '@/types/common/base'

/**
 * 系统服务-角色查询类
 */
export interface SysRoleParam {
  roleName: string
  enabled: boolean
}

/**
 * 系统服务-角色实体类
 */
export interface SysRole extends BaseEntity {
  roleId: string
  roleName: string
  enabled: boolean
  delFlag: boolean
}

/**
 * 系统服务-角色修改类
 */
export interface SysRoleDTO {
  roleName: string
  enabled: boolean
}

/**
 * 系统服务-角色返回结果类
 */
export interface SysRoleVO {
  roleId: string
  roleName: string
  enabled: boolean
  delFlag: boolean
  createUser: string
  createTime: string
  updateUser: string
  updateTime: string
}
