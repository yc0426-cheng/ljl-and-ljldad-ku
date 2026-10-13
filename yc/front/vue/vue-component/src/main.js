import { createApp } from 'vue'
import App from './App.vue'
import Header from './pages/header.vue'

const app = createApp(App)

// 全局引用位置
app.component('Header', Header)

app.mount('#app')
