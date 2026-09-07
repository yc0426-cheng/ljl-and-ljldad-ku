import { defineStore } from 'pinia' // pinia 状态仓库定义函数
import { login as loginApi, logout as logoutApi, checkToken } from '@/api/auth' // 登录/登出/token 校验接口

// 用户信息结构：与后端 LoginUserInfo（userId / account / name / token）对应
export interface UserInfo {
  /** 用户 id */
  userId?: number
  /** 账号 */
  account: string
  /** 名称 */
  name?: string
  /** token */
  token?: string
  /** 头像 */
  avatar?: string
}

/**
 * 用户状态仓库：全局唯一，管理登录态与 token
 * 任何组件里通过 useUserStore() 获取同一个实例
 */
export const useUserStore = defineStore('user', {
  // ---------------- 状态 ----------------
  state: () => ({
    // 优先从 localStorage 读取，实现刷新页面后登录态不丢失
    token: localStorage.getItem('token') || '',
    // 用户信息；JSON.parse 前先兜底 'null'，避免 localStorage 为空时报错
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') as UserInfo | null
  }),

  // ---------------- 计算属性 ----------------
  getters: {
    // 是否已登录（有 token 即认为已登录）
    isLoggedIn: (state): boolean => !!state.token
  },

  // ---------------- 动作 ----------------
  actions: {
    /**
     * 登录：调后端接口，成功后保存 token 与用户信息
     * @param payload 登录表单
     * @param payload.account 账号
     * @param payload.password 密码
     */
    async login({ account, password }: { account: string; password: string }): Promise<void> {
      // 调后端 /auth/login，返回 token 字符串
      const token = await loginApi(account, password)

      // 保存 token：内存 state + localStorage 持久化（两者保持一致）
      this.token = token
      localStorage.setItem('token', token)

      // 先用账号占位保存（后端登录接口只返回 token）
      this.userInfo = { account }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))

      // 尽力补齐用户信息（userId/name）：登录接口没返回用户信息，
      // 这里复用 /auth/check 再取一次；失败不影响登录（已有 account 可用）
      try {
        const info = await checkToken()
        if (info) {
          this.setUserInfo(info)
        }
      } catch {
        // 忽略：基础信息已足够展示，不因补全失败而登录失败
      }
    },

    /**
     * 更新/合并当前用户信息（内存 + localStorage 持久化）
     * 供 /auth/check 通过后回写真实 userId/name，保证刷新后页面数据不丢。
     * 注意：不覆盖 token（token 单独维护在 this.token）
     * @param info 后端返回的用户信息（Partial，仅更新给定字段）
     */
    setUserInfo(info: Partial<UserInfo>): void {
      // 合并后仅用于展示；account 在登录/守卫校验流程中必有兜底值
      this.userInfo = { ...this.userInfo, ...info } as UserInfo
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    /**
     * 清除本地登录态（token 校验失败被守卫踢出时使用）
     * 与 logout 的区别：不调后端接口，只清内存 state 与 localStorage
     */
    clearToken(): void {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    /**
     * 登出：调后端接口并清除本地登录态
     */
    async logout(): Promise<void> {
      // 调后端登出接口；后端异常/网络失败时 try 住，不影响本地清理
      try {
        await logoutApi()
      } catch {
        // 忽略：本地清理照常执行
      }
      // 清空 token 与用户信息（内存 + localStorage）
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
