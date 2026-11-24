import request from "@/api/request";
import type { LoginDTO } from "@/types/auth";
import type { LoginUserInfo } from "@/types/common";

/**
 * 认证服务Api
 */
export class AuthApi {
  static login(loginDTO: LoginDTO): Promise<string> {
    return request.post("/auth/login", loginDTO);
  }

  static getErrorCount(account: string): Promise<number> {
    return request.post("/auth/error-count", account);
  }

  static check(authorization: string): Promise<LoginUserInfo> {
    return request.post("/auth/check", authorization);
  }

  static logout(authorization: string): Promise<void> {
    return request.post("/auth/logout", authorization);
  }
}
