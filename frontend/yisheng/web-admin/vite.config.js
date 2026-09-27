import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')
  const port = Number(env.VITE_PORT || 3002)
  const legacyApiTarget = env.VITE_API_BASE_URL || 'http://localhost:48080'
  const backendV2Target = env.VITE_BACKEND_V2_TARGET || 'http://127.0.0.1:8080'
  const backendV2Proxy = {
    target: backendV2Target,
    changeOrigin: true,
    secure: false,
    configure(proxy) {
      proxy.on('proxyReq', (request) => request.removeHeader('origin'))
    }
  }

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
        '/admin-api/system/auth': backendV2Proxy,
        '/admin-api/notifications': backendV2Proxy,
        '/ws/admin-notifications': {
          target: backendV2Target,
          changeOrigin: true,
          secure: false,
          ws: true
        },
        '/admin-api/help-orders': backendV2Proxy,
        '/admin-api': {
          target: legacyApiTarget,
          changeOrigin: true,
          secure: false
        }
      }
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            'vendor-vue': ['vue', 'vue-router', 'pinia'],
            'vendor-element': ['element-plus', '@element-plus/icons-vue'],
            'vendor-axios': ['axios']
          }
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
