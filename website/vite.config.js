import { defineConfig } from 'vite'

export default defineConfig({
    base: '/Yung-Music/', // Correct base path for GitHub Pages sub-directory
    build: {
        outDir: 'dist',
    }
})
