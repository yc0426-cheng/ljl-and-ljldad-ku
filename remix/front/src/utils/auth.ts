import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 被动登出选项
 */
export interface LogoutOptions {
  /** 是否先弹确认框（true 时用户点"重新登录"后才跳转，用于会话失效/越权场景） */
  needConfirm?: boolean
  /** 是否同步关闭其他标签页（本项目暂无多标签逻辑，预留字段） */
  closeOtherTabs?: boolean
}

/**
 * 强制登出：清除本地登录态并跳回登录页
 *
 * 供响应拦截器（401 / 403 / 危险级 500）与会话失效场景调用，
 * 与 store/user.ts 的 logout（主动登出，会调后端接口）区分开：
 * 这里是"被动踢出"，只做本地清理与跳转。
 *
 * @param message 提示信息
 * @param options 选项，见 LogoutOptions
 */
export function logout(message = '登录已失效', options: LogoutOptions = {}): void {
  const { needConfirm } = options

  // 清除本地登录态（key 与 store/user.ts 保持一致）
  const clear = (): void => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  if (needConfirm) {
    // 会话失效类：弹提示框，用户点"重新登录"后再清理并跳转
    ElMessageBox.alert(message, '提示', {
      confirmButtonText: '重新登录',
      type: 'warning'
    })
      .then(() => {
        clear()
        location.href = '/login'
      })
      .catch(() => {
        // 用户关闭弹窗：不强制跳转，保持当前页面
      })
  } else {
    // 普通提示：先提示再清理跳转
    if (message) {
      ElMessage.error(message)
    }
    clear()
    // 避免已在登录页时重复跳转
    if (!location.pathname.startsWith('/login')) {
      location.href = '/login'
    }
  }
}
