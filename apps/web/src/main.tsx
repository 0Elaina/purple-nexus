import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { loadRuntimeConfig } from './config/runtimeConfig.ts'

/*
 * React 挂载前完成运行配置校验。
 * 如果 API 地址缺失或非法，应用会立即停止并给出明确错误。
 */
loadRuntimeConfig()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
