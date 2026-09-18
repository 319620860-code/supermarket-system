import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:8080'

  return {
    base: '/',
    plugins: [vue()],
    resolve: {
      alias: {
        '@': '/src'
      }
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      logLevel: 'verbose',
      historyApiFallback: {
        rewrites: [
          { from: /^\/$/, to: '/index.html' },
          { from: /^\/login/, to: '/index.html' },
          { from: /^\/home/, to: '/index.html' },
          { from: /.*/, to: '/index.html' }
        ]
      },
      allowedHosts: ['localhost', '127.0.0.1', '0.0.0.0', '192.168.2.5', '138b4766.r27.cpolar.top', '49c4d304.r27.cpolar.top'],
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          ws: true,
          secure: false
        }
      }
    },
    build: {
      chunkSizeWarningLimit: 1300,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('node_modules')) return
            if (id.includes('echarts')) return 'echarts'
            if (id.includes('element-plus') || id.includes('@element-plus')) return 'element-plus'
            if (id.includes('vue-router')) return 'vue-router'
            if (/node_modules\/vue\//.test(id)) return 'vue-core'
          }
        }
      }
    }
  }
})
