/**
 * 系统服务-用户角色返回结果类
 */
export interface SysUserRoleVO {
  /** 用户绑定角色ID */
  userRoleId: number
  /** 用户id */
  userId: number
  /** 角色ID */
  roleId: number
}

/**
 * 系统服务-用户角色分配入参类
 */
export interface SysUserRoleAssignDTO {
  /** 被分配角色的用户id（必填） */
  userId: number
  /** 角色id集合；空集合 = 清空该用户全部角色 */
  roleIds: number[]
}
