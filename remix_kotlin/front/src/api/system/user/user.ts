import type {
  SysUserDTO,
  SysUserFeignDTO,
  SysUserParam,
  SysUserVO,
} from "@/types/system/user";
import request from "@/api/request.ts";
import type { LoginUserInfo } from "@/types/common";

export class SysUserApi {
  static getUserInfoByAccount(account: string): Promise<SysUserFeignDTO> {
    return request.get("/sys/user/get/account", { account: account });
  }

  static editError(userId: string): Promise<void> {
    return request.post("sys/user/edit/error", userId);
  }

  static editLogin(userId: string): Promise<void> {
    return request.post("sys/user/edit/login", userId);
  }

  static getLoginUserInfo(userId: string): Promise<LoginUserInfo> {
    return request.get("/sys/user/get/login/info", { userId: userId });
  }

  static setLastLoginTime(userId: string): Promise<void> {
    return request.post("sys/user/last-login-time", userId);
  }

  static page(): Promise<SysUserVO[]> {
    return request.post("sys/user/page");
  }

  static list(param: SysUserParam): Promise<SysUserVO[]> {
    return request.get("sys/user/list", param);
  }

  static add(dto: SysUserDTO): Promise<void> {
    return request.post("sys/user/add", dto);
  }

  static edit(dto: SysUserDTO): Promise<void> {
    return request.post("sys/user/edit", dto);
  }
}
