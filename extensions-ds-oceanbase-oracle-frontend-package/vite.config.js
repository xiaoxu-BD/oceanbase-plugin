import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    lib: {
      entry: 'src/entry.js',
      formats: ['iife'],
      name: 'OceanBaseOraclePluginBundle',
      fileName: () => 'extensions-ds-oceanbase-oracle.js'
    },
    rollupOptions: {
      output: {
        inlineDynamicImports: true
      }
    }
  }
})
