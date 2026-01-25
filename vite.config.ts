import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    outDir: 'target/war',
    emptyOutDir: false, // Don't delete existing GWT files
    rollupOptions: {
      external: ['/javac-api.js'],
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  },
  server: {
    port: 3000,
    proxy: {
      // Proxy requests to GWT-generated files during dev
      '/jib': 'http://localhost:8080',
      '/javac': 'http://localhost:8080'
    }
  }
})
