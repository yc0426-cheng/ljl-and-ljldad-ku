import { fileURLToPath, URL } from 'node:url' // node:url：把相对路径转成文件 URL，用于配置 @ 别名
import { defineConfig, type Plugin } from 'vite' // vite 配置函数 + 插件类型
import vue from '@vitejs/plugin-vue' // vue 单文件组件(SFC)编译插件

/**
 * 虚拟 index.html 插件
 * 项目要求：不允许使用任何 .html 文件，因此仓库中不存放 index.html，
 * 而是由本插件在运行时动态生成 HTML：
 *  - 开发模式：拦截根路径("/")请求，直接返回生成的 HTML
 *  - 生产构建：把生成的 HTML 作为产物文件输出到 dist/index.html
 */
function virtualIndexHtml(): Plugin {
  // 生成 HTML 内容；scriptSrc 指向入口文件（dev 为 ts，build 为打包后的 js），styleLinks 为打包后的 css
  const html = (scriptSrc: string, styleLinks = ''): string => `<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Remix 学习管理系统</title>
    ${styleLinks}
  </head>
  <body>
    <!-- Vue 应用挂载点：main.ts 中 app.mount('#app') 会渲染到这里 -->
    <div id="app"></div>
    <script type="module" src="${scriptSrc}"></script>
  </body>
</html>`

  return {
    name: 'virtual-index-html',

    // 开发模式：用中间件拦截根路径请求，直接返回生成的 HTML
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = (req.url ?? '').split('?')[0] // 去掉查询参数
        if (url === '/' || url === '/index.html') {
          res.statusCode = 200
          res.setHeader('Content-Type', 'text/html; charset=utf-8')
          res.end(html('/src/main.ts')) // 入口指向 src/main.ts，由 vite 按需编译
          return
        }
        next()
      })
    },

    // 生产构建：把生成的 HTML 作为产物输出（引用打包后的 js 入口与 css 资源）
    generateBundle(_options, bundle) {
      // 找到入口 chunk（打包后的 JS 文件）
      const entry = Object.values(bundle).find((item) => item.type === 'chunk' && item.isEntry)
      // 收集样式文件（.vue 组件样式会被 vite 提取为独立 css 资源）
      const cssFiles = Object.values(bundle)
        .filter((item) => item.type === 'asset' && item.fileName.endsWith('.css'))
        .map((item) => `/${item.fileName}`)
      if (entry && entry.type === 'chunk') {
        // 把 css 拼成 <link> 标签（当前无样式时为空字符串）
        const styleLinks = cssFiles
          .map((href) => `<link rel="stylesheet" href="${href}" />`)
          .join('\n    ')
        this.emitFile({
          type: 'asset',
          fileName: 'index.html',
          source: html(`/${entry.fileName}`, styleLinks)
        })
      }
    }
  }
}

export default defineConfig({
  // 注册插件：vue 编译 + 虚拟 HTML
  plugins: [vue(), virtualIndexHtml()],

  // 路径解析配置
  resolve: {
    alias: {
      // 配置 @ 指向 src 目录，import 写法更简洁，例如：import request from '@/utils/request'
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },

  // 开发服务器配置
  server: {
    port: 5173,
    proxy: {
      // 开发环境代理：把 /auth 开头的请求转发到后端 auth 服务(端口 11000)，
      // 从而避免前后端跨域问题。生产环境则由 nginx 等反向代理处理。
      '/auth': {
        target: 'http://localhost:11000',
        changeOrigin: true
      }
    }
  },

  // 生产构建配置
  build: {
    rollupOptions: {
      // 项目不存放 index.html，构建入口直接指向 src/main.ts；
      // 页面 HTML 由上面的虚拟 HTML 插件在打包时生成
      input: 'src/main.ts'
    }
  }
})
