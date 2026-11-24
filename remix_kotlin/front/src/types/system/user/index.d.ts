import type { SysUserStatusEnum } from "./constants";

export interface SysUserParam {
  account: string;
  name: string;
}

export interface SysUserDTO {
  userId: string;
  account: string;
  name: string;
  phone: string;
  idNumber: string;
  email: string;
  status: SysUserStatusEnum;
}

export interface SysUserVO {
  userId: string;
  account: string;
  name: string;
  phone: string;
  idNumber: string;
  email: string;
  passErrorCount: number;
  status: SysUserStatusEnum;
  lastLoginTime: string;
  delFlag: boolean;
}

export interface SysUserFeignDTO {
  userId: string;
  account: string;
  name: string;
  phone: string;
  idNumber: string;
  email: string;
  passErrorCount: number;
  status: SysUserStatusEnum;
  lastLoginTime: string;
  delFlag: boolean;
}
