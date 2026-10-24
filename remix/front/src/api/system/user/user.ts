import { SysUserDTO, SysUserParam, SysUserVO } from '@/types/system/user'
import { request } from '@/utils/request'

/**
 * 系统用户Api
 * 注：后端 SysUserController#add/#edit 入参无 @RequestBody，参数由 query 绑定，
 * 故以 query 传参；后端补 @RequestBody 后可改为 JSON body。
 * 后端暂无 /sys/user/delete 接口，删除能力待后端提供
 */
export class SysUserApi {
  /**
   * 分页用户信息
   *
   * @return 用户列表
   */
  static page(): Promise<SysUserVO[]> {
    return request.post('/sys/user/page')
  }

  /**
   * 获取用户信息列表
   * 注：后端 SysUserController#list 是 @GetMapping（参数走 query），须用 GET 请求
   *
   * @param param 查询参数
   * @return 用户列表
   */
  static list(param: SysUserParam): Promise<SysUserVO[]> {
    return request.get('/sys/user/list', { params: param })
  }

  /**
   * 新增用户
   *
   * @param dto 新增用户数据
   */
  static add(dto: SysUserDTO): Promise<void> {
    return request.post('/sys/user/add', undefined, { params: { ...dto } })
  }

  /**
   * 修改用户
   *
   * @param dto 修改用户信息
   */
  static edit(dto: SysUserDTO): Promise<void> {
    return request.post('/sys/user/edit', undefined, { params: { ...dto } })
  }
}
