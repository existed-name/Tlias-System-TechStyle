import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
  ],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        secure: false,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      }
    }
  },

  // 新增：自定义构建输出目录
  build: {
    // outDir: 'dist',
    outDir: 'tlias-management-frontend',   // 输出为这个名字，而不是默认的 dist
    emptyOutDir: true                      // 每次 build 前清空旧文件，避免旧文件残留
    // sourcemap: false          // 生产通常关闭
  }

})
