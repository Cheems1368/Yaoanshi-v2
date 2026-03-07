import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')
  const port = Number(env.VITE_PORT || 3002)
  const legacyApiTarget = env.VITE_API_BASE_URL || 'http://localhost:48080'
  const backendV2Target = env.VITE_BACKEND_V2_TARGET || 'http://127.0.0.1:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port,
      strictPort: true,
      open: true,
      proxy: {
        '/ws/admin-notifications': {
          target: backendV2Target,
          changeOrigin: true,
          secure: false,
          ws: true
        },
        '/admin-api/help-orders': {
          target: backendV2Target,
          changeOrigin: true,
          secure: false
        },
        '/admin-api': {
          target: legacyApiTarget,
          changeOrigin: true,
          secure: false
        }
      }
    },
    define: {
      'process.env': {
        VITE_API_BASE_URL: JSON.stringify(legacyApiTarget),
        VITE_BACKEND_V2_API_BASE_URL: JSON.stringify(env.VITE_BACKEND_V2_API_BASE_URL || ''),
        VITE_BACKEND_V2_WS_BASE_URL: JSON.stringify(env.VITE_BACKEND_V2_WS_BASE_URL || '')
      }
    }
  }
})
