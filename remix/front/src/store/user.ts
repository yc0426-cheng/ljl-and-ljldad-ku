import { defineStore } from 'pinia' // pinia 状态仓库定义函数
import { login as loginApi, logout as logoutApi } from '@/api/auth' // 登录/登出接口

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

      // 后端登录接口目前只返回 token，没有用户信息；
      // 后续可增加 /auth/userInfo 之类接口获取后填到这里
      this.userInfo = { account }
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
      // 调后端登出接口；后端暂未实现会抛错，try 住不影响本地清理
      try {
        await logoutApi()
      } catch {
        // 忽略：登出接口未实现时，本地清理照常执行
      }
      // 清空 token 与用户信息（内存 + localStorage）
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
