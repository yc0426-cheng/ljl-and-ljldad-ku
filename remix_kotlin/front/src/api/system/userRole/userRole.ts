import request from "@/api/request.ts";
import type { SysUserRoleVO } from "@/types/system/userRole";

export class SysUserRoleApi {
  static bindRole(userId: string, roleIdList: string[]): Promise<void> {
    return request.post("sys/user/role/bind", roleIdList, {
      params: { userId },
    });
  }

  static page(userId: string): Promise<SysUserRoleVO> {
    return request.post("sys/user/role/bind", userId);
  }
}
