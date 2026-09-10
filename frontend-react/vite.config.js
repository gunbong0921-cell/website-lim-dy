/**
 * Hexaq
 * 계층: App
 * 객체: vite.config
 * 책임: React 정적 빌드. same-origin-assets 가 같은 출처 JS·CSS 의 crossorigin 을 뺀다
 * 문서: [docs/technical/03-cloudflare-tunnel.md](../docs/technical/03-cloudflare-tunnel.md) · [docs/technical/01-architecture.md](../docs/technical/01-architecture.md)
 */
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { fileURLToPath, URL } from 'node:url'


// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    {
      name: 'same-origin-assets',
      transformIndexHtml(html) {
        return html
          .replaceAll('<script type="module" crossorigin', '<script type="module"')
          .replaceAll('rel="stylesheet" crossorigin href="./assets', 'rel="stylesheet" href="./assets')
          .replaceAll('rel="stylesheet" crossorigin href="/assets', 'rel="stylesheet" href="/assets')
      },
    },
  ],
  // npm run build 실행시, outDir로 지정한 경로에 빌드된 결과물이 생성됨
  build: {
    outDir: fileURLToPath(
      new URL('../src/main/resources/static', import.meta.url)
    ),
    emptyOutDir: true,
  },
  // 배포했을때 정적 파일 경로를 상대경로로 설정
  base: './',
  server: {
    host: '127.0.0.1',
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8282',
        changeOrigin: true,
      },
    },
  },
})

