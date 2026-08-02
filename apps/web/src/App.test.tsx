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

        // 三个首页场景必须按既定顺序存在，动效只能增强它们，不能替换语义结构。
        expect(document.querySelector('main > .home-hero')).toBeInTheDocument()
        expect(document.getElementById('nexus-transition')).toBeInTheDocument()
        expect(document.getElementById('gateway')).toBeInTheDocument()
        expect(
            document.querySelector('.home-nexus__bridge'),
        ).toBeInTheDocument()

        expect(
            screen.getByRole('heading', {
                level: 2,
                name: /视觉与交互/,
            }),
        ).toBeInTheDocument()

        for (const entryTitle of ['Projects', 'Blog', 'Agent']) {
            expect(
                screen.getByRole('heading', {
                    level: 3,
                    name: entryTitle,
                }),
            ).toBeInTheDocument()

            expect(
                screen.getByRole('article', {
                    name: entryTitle,
                }),
            ).toBeInTheDocument()

            expect(
                screen.queryByRole('link', {
                    name: new RegExp(entryTitle),
                }),
            ).not.toBeInTheDocument()
        }

        expect(screen.getAllByText('建设中')).toHaveLength(3)

        expect(
            screen.getByRole('link', {
                name: '跳至主要内容',
            }),
        ).toHaveAttribute('href', '#main-content')

        expect(
            screen.getByRole('link', {
                name: 'Purple Nexus',
            }),
        ).toHaveAttribute('aria-current', 'page')

        expect(
            screen.getByRole('link', {
                name: '跳至主要内容',
            }),
        ).toHaveClass('focus-visible:translate-y-0')

        // 建设中入口必须保持非链接语义，不能意外进入键盘交互路径。
        const gateway = document.getElementById('gateway')
        expect(gateway?.querySelector('.home-gateway__grid')).toBeInTheDocument()
        expect(gateway?.querySelectorAll('.home-gateway__card')).toHaveLength(3)
        expect(gateway?.querySelectorAll('article')).toHaveLength(3)
        expect(gateway?.querySelectorAll('article a')).toHaveLength(0)

        // 媒体只承担装饰职责；即使无法播放，页面仍依靠可见标题和说明完成阅读。
        const heroMedia = document.querySelector('.home-hero__media')
        expect(heroMedia).toHaveAttribute('aria-hidden', 'true')
        expect(heroMedia).not.toHaveAttribute('autoplay')
        expect(heroMedia).toHaveAttribute('preload', 'metadata')
        expect(heroMedia?.querySelector('source')).toHaveAttribute(
            'src',
            '/assets/home/hero-plana-loop.mp4',
        )

        // 跳过链接的目标必须是可聚焦的主要内容容器，避免固定 Header 遮挡正文入口。
        expect(screen.getByRole('main')).toHaveAttribute('tabindex', '-1')

        expect(
            screen.getByText(/个人非商业、非官方的视觉与技术实验项目/),
        ).toBeInTheDocument()
    })
})
