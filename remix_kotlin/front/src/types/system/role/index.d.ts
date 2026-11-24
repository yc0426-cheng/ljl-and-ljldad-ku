import type { BaseEntity } from "@/types/common";

export interface SysRole extends BaseEntity {
  roleId: string;
  roleName: string;
  enabled: number;
  delFlag: number;
}

export interface SysRoleParam {
  roleName: string;
  enabled: number;
}

export interface SysRoleDTO {
  roleName: string;
  enabled: number;
}

export interface SysRoleVO {
  roleId: string;
  roleName: string;
  enabled: number;
  delFlag: number;
  createUser: string;
  createTime: string;
  updateUser: string;
  updateTime: string;
}
