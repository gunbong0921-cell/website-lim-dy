/**
 * Hexaq
 * 계층: App
 * 객체: main
 * 책임: main 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/technical/05-responsive.md](../../docs/technical/05-responsive.md) · [docs/technical/01-architecture.md](../../docs/technical/01-architecture.md)
 */
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { HashRouter } from 'react-router-dom'
import App from './App.jsx'
import './theme/landed/css/main.css'
import './styles/hexaq.css'
import './styles/hexaq-compact.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <HashRouter>
      <App />
    </HashRouter>
  </StrictMode>,
)
