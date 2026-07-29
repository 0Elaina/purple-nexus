import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

/**
 * 在每个测试用例之后卸载 React 组件并清理 DOM
 */
afterEach(() => {
    cleanup()
})