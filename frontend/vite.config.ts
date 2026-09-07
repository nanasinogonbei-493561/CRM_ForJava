import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // /api で始まるリクエストを backend に転送
      '/api': {
        target: 'http://localhost:8080', // backend のポートに合わせる
        changeOrigin: true,
        // backend 側に /api プレフィックスが無いなら↓を有効化
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
