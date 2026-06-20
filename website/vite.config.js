import { defineConfig } from 'vite'

export default defineConfig({
    base: '/', // Base path for Vercel/Root domain deployment
    build: {
        outDir: 'dist',
    }
})
