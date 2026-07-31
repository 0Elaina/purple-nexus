import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('App', () => {
    it('renders the accessible Purple Nexus home shell', () => {
        // Given / When：从真实应用入口渲染当前首页。
        render(<App />)

        // Then：页面具有完整且可识别的外壳语义。
        expect(screen.getByRole('banner')).toBeInTheDocument()
        expect(screen.getByRole('main')).toBeInTheDocument()
        expect(screen.getByRole('contentinfo')).toBeInTheDocument()

        expect(
            screen.getByRole('heading', {
                level: 1,
                name: 'Purple Nexus',
            }),
        ).toBeInTheDocument()

        expect(
            screen.getByRole('link', {
                name: '跳至主要内容',
            }),
        ).toHaveAttribute('href', '#main-content')

        expect(
            screen.getByText(/个人非商业、非官方的视觉与技术实验项目/),
        ).toBeInTheDocument()
    })
})