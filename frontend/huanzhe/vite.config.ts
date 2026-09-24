/// <reference types="vitest" />

import legacy from '@vitejs/plugin-legacy'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { defineConfig, loadEnv } from 'vite'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')
  const apiTarget = env.VITE_APP_API_TARGET || 'http://localhost:8080'

  return {
    plugins: [
      vue(),
      legacy()
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@/picture': path.resolve(__dirname, './src/shared/assets/images'),
      },
    },
    server: {
      port: 3001,
      strictPort: true,
      host: '127.0.0.1',
      proxy: {
        '/app-api': {
          target: apiTarget,
          changeOrigin: true,
          secure: false
        }
      }
    },
    define: {
      __API_BASE_URL__: JSON.stringify(apiTarget),
      __API_PREFIX__: JSON.stringify('/app-api')
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            'vendor-vue': ['vue', 'vue-router'],
            'vendor-ionic': ['@ionic/vue', '@ionic/vue-router'],
            'vendor-axios': ['axios']
          }
        }
      }
    },
    test: {
      globals: true,
      environment: 'jsdom'
    }
  }
})
