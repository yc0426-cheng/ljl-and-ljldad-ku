import request from "@/api/request";
import type {
  SysRole,
  SysRoleDTO,
  SysRoleParam,
  SysRoleVO,
} from "@/types/system/role";

export class SysRoleApi {
  static list(param: SysRoleParam): Promise<SysRoleVO> {
    return request.post("/sys/role/list", param);
  }

  static addRole(dto: SysRoleDTO): Promise<void> {
    return request.post("/sys/role/add", dto);
  }

  static updateRole(dto: SysRoleDTO): Promise<void> {
    return request.post("/sys/role/update", dto);
  }

  static deleteRole(roleId: string): Promise<void> {
    return request.post("/sys/role/update", roleId);
  }

  static exportRole(list: SysRole[]): Promise<void> {
    return request.download("/sys/role/export", list, "角色信息表.xlsx");
  }
}
