import { request } from '@/utils/request'
import { SysUserDTO, SysUserParam, SysUserVO } from '@/types/system/user'

/**
 * 系统用户Api
 */
export class SysUserApi {
  /**
   * 分页查询用户信息
   */
  static page = (): Promise<SysUserVO[]> => {
    return request.post('/sys/user/page')
  }

  /**
   * 查询用户信息
   * 注：后端 SysUserController#list 是 @GetMapping（参数走 query），须用 GET 请求
   */
  static list = (param: SysUserParam): Promise<SysUserVO[]> => {
    return request.get('/sys/user/list', { params: param })
  }

  /**
   * 新增用户
   */
  static add = (dto: SysUserDTO): Promise<void> => {
    return request.post('/sys/user/add', dto)
  }

  /**
   * 删除用户
   */
  static delete = (dto: SysUserDTO): Promise<void> => {
    return request.post('/sys/user/delete', dto)
  }

  /**
   * 编辑用户
   */
  static edit = (dto: SysUserDTO): Promise<void> => {
    return request.post('/sys/user/edit', dto)
  }
}
