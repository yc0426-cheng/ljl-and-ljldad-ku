import {fileURLToPath, URL} from 'node:url' // node:url：把相对路径转成文件 URL，用于配置 @ 别名
import {defineConfig, type Plugin} from 'vite' // vite 配置函数 + 插件类型
import vue from '@vitejs/plugin-vue' // vue 单文件组件(SFC)编译插件

/**
 * 虚拟 index.html 插件
 * 项目要求：不允许使用任何 .html 文件，因此仓库中不存放 index.html
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
    <link rel="icon" href="/favio.ico">
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

                // 后端 API 走 /api 前缀：放行给 vite server.proxy 转发到后端，不能被 SPA fallback 拦截
                if (url.startsWith('/api')) {
                    next()
                    return
                }

                // 支持 SPA 历史模式：除了静态资源，所有路径都返回 index.html
                // 静态资源包括：.js, .css, .png, .jpg, .svg, .ico, .woff, .ttf, .json 等
                const isStatic = /\.(js|css|png|jpg|jpeg|gif|svg|ico|woff|ttf|json|map)$/.test(url)
                const isViteInternal = url.startsWith('/@')
                    || url.startsWith('/node_modules')
                    || url.startsWith('/src/')
                    || url === '/main.ts'

                if (url === '/favicon.ico') {
                    res.statusCode = 204
                    res.end()
                    return
                }

                if (!isStatic && !isViteInternal) {
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
        host: '127.0.0.1',  // 显式绑定 IPv4，避免 Node.js 18+ 在 Windows 上默认只绑 [::1] 导致 Postman/浏览器走 IPv4 连不上
        port: 10000,
        proxy: {
            // 开发环境代理：把 /api 开头的请求转发到后端 auth 服务(端口 11000)
            // 通过 rewrite 去掉 /api 前缀，让后端按 /auth/login 等原始路径接收
            '/api': {
                target: 'http://localhost:11000',
                changeOrigin: true,
                ws: true,
                rewrite: (path) => path.replace(/^\/api/, ''),
                configure: (proxy) => {
                    proxy.on('proxyReq', (proxyReq, req) => {
                        // 透传客户端真实 IP 给后端（gateway 上线后此逻辑可迁移到 gateway）
                        const clientIp = req.socket.remoteAddress
                        if (clientIp) {
                            proxyReq.setHeader('X-Real-IP', clientIp)
                        }
                    })
                }
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
