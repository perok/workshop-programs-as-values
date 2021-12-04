import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
const path = require('path')

export default defineConfig(({ command, mode }) => {
  // TODO difference between command === 'server'
  // TODO configure index.html with correct scala.js build file
  return {
    resolve: {
      alias: {
        '@css-sources': path.resolve(__dirname, './src/main/css'),
      }
    },
    plugins: [react()]
  }
})

