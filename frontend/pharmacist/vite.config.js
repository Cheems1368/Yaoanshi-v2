import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')
  const port = Number(env.VITE_PORT || 3003)
  const legacyApiTarget = env.VITE_API_BASE_URL || 'http://localhost:48080'
  const backendV2Target = env.VITE_BACKEND_V2_TARGET || 'http://127.0.0.1:8080'
  const backendV2AdminPrefixes = [
    '/admin-api/system',
    '/admin-api/patients',
    '/admin-api/pharmacists',
    '/admin-api/prescriptions',
    '/admin-api/ddi',
    '/admin-api/medication-plans',
    '/admin-api/help-orders',
    '/admin-api/notifications'
  ]

  const proxy = {}

  backendV2AdminPrefixes.forEach((prefix) => {
    proxy[prefix] = {
      target: backendV2Target,
      changeOrigin: true,
      secure: false,
      configure(proxy) {
        proxy.on('proxyReq', (request) => request.removeHeader('origin'))
      }
    }
  })

  proxy['/ws/admin-notifications'] = {
    target: backendV2Target,
    changeOrigin: true,
    secure: false,
    ws: true
  }

  proxy['/admin-api'] = {
    target: legacyApiTarget,
    changeOrigin: true,
    secure: false
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
      proxy
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
        VITE_BACKEND_V2_WS_BASE_URL: JSON.stringify(env.VITE_BACKEND_V2_WS_BASE_URL || ''),
        VITE_APP_TITLE: JSON.stringify(env.VITE_APP_TITLE || 'Pharmacist Web')
      }
    }
  }
})
