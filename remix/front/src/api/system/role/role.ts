import { SysRole, SysRoleDTO, SysRoleParam, SysRoleVO } from '@/types/system/role'
import { request } from '@/utils/request'

/**
 * 系统角色Api
 */
export class RoleApi {
  /**
   * 查询角色
   *
   * @param param 查询参数
   * @return 角色列表
   */
  static list(param: SysRoleParam): Promise<SysRoleVO[]> {
    return request.post('/sys/role/list', param)
  }

  /**
   * 新建角色
   *
   * @param dto 新增角色数据
   */
  static addRole(dto: SysRoleDTO): Promise<void> {
    return request.post('/sys/role/add', dto)
  }

  /**
   * 修改角色
   *
   * @param dto 修改角色数据
   */
  static updateRole(dto: SysRoleDTO): Promise<void> {
    return request.post('/sys/role/update', dto)
  }

  /**
   * 删除角色
   *
   * @param roleId 角色id
   */
  static deleteRole(roleId: string): Promise<void> {
    return request.post('/sys/role/delete', roleId)
  }

  /**
   * 导出角色信息
   *
   * @param list  导出数据
   */
  static exportRole(list: SysRole[]): Promise<void> {
    return request.download('/sys/role/export', '角色消息表.xlsx', 'POST', list)
  }
}
