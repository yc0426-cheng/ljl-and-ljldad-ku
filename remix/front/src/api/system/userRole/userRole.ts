import { SysUserRoleAssignDTO, SysUserRoleVO } from '@/types/system/userRole'
import { request } from '@/utils/request'

/**
 * 系统用户角色Api
 */
export class UserRoleApi {
  /**
   * 绑定角色
   * ⚠️ 对应后端 userRole 包 SysUserRoleController#bindRoles，
   * 该方法声明了两个 @RequestBody（后端问题），无法正常接收参数；
   * 前端按约定提交 {userId, roleIdList} 对象，后端改为单个 DTO 接收后即可联通
   *
   * @param userId     用户id
   * @param roleIdList 角色id列表
   */
  static bind(userId: number, roleIdList: number[]): Promise<void> {
    return request.post('/sys/user/role/bind', { userId, roleIdList })
  }

  /**
   * 查询列表
   *
   * @param userId 用户id
   * @return 用户绑定角色列表
   */
  static page(userId: number): Promise<SysUserRoleVO[]> {
    return request.post('/sys/user/role/page', userId)
  }

  /**
   * 为用户分配角色（全量分配：先清空旧角色再写入新角色）
   * 对应后端 user 包 SysUserRoleController#assignRoles；
   * roleIds 传空数组 = 清空全部角色
   *
   * @param dto 分配数据：{userId, roleIds}
   */
  static assign(dto: SysUserRoleAssignDTO): Promise<void> {
    return request.post('/sys/user/role/assign', dto)
  }
}
