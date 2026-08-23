import { createApp } from 'vue' // Vue 3 应用创建函数
import { createPinia } from 'pinia' // 状态管理：全局共享登录态/token
import App from './App.vue' // 根组件
import router from './router' // 路由（含登录守卫）

// 创建 Vue 应用实例
const app = createApp(App)

// 按顺序挂载插件：先 pinia(状态) 后 router(路由)。
// 路由守卫里会用到 user store，因此 pinia 必须先注册。
app.use(createPinia())
app.use(router)

// 把应用挂载到 index.html 里的 #app 节点
// （index.html 由 vite.config.ts 中的虚拟 HTML 插件动态生成，仓库中无 .html 文件）
app.mount('#app')
