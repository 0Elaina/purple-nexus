import { useGSAP } from '@gsap/react'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import type { RefObject } from 'react'

// React 适配器不依赖浏览器视口，可以在模块加载时安全注册。
gsap.registerPlugin(useGSAP)

const DESKTOP_MOTION_QUERY =
    '(min-width: 1024px) and (prefers-reduced-motion: no-preference)'

/**
 * 管理首页桌面滚动叙事的统一生命周期。
 *
 * 动画只增强既有静态场景，不负责生成内容或改变入口状态；
 * useGSAP 的作用域与 matchMedia 的还原共同保证组件卸载、断点变化时清理内联样式和 ScrollTrigger。
 */
export function useHomeMotion(scope: RefObject<HTMLDivElement | null>) {
    useGSAP(
        () => {
            // 测试环境或旧浏览器缺少 matchMedia 时保留完整静态页面。
            if (
                typeof window === 'undefined' ||
                typeof window.matchMedia !== 'function'
            ) {
                return
            }

            // ScrollTrigger 注册时会立即读取 matchMedia，因此必须放在浏览器能力检查之后。
            gsap.registerPlugin(ScrollTrigger)

            const media = gsap.matchMedia()

            media.add(DESKTOP_MOTION_QUERY, () => {
                const heroMedia = scope.current?.querySelector<HTMLVideoElement>(
                    '.home-hero__media',
                )
                const nexusBridge = scope.current?.querySelector<HTMLElement>(
                    '.home-nexus__bridge',
                )

                // 视频只是桌面非 Reduced Motion 下的装饰层；播放失败不应阻塞正文。
                void heroMedia?.play().catch(() => undefined)

                const transitionTimeline = gsap.timeline({
                    scrollTrigger: {
                        trigger: '.home-hero',
                        start: 'top top',
                        end: 'bottom top',
                        scrub: 0.8,
                        invalidateOnRefresh: true,
                    },
                })

                transitionTimeline
                    .to(
                        '.home-hero__media',
                        {
                            opacity: 0.28,
                            scale: 1.08,
                            yPercent: 5,
                            ease: 'none',
                        },
                        0,
                    )
                    .to(
                        '.home-hero__content',
                        {
                            opacity: 0,
                            yPercent: -18,
                            ease: 'none',
                        },
                        0,
                    )
                    .fromTo(
                        '.home-nexus__content',
                        {
                            opacity: 0,
                            y: 72,
                        },
                        {
                            opacity: 1,
                            y: 0,
                            ease: 'none',
                        },
                        0.55,
                    )
                // The bridge is a structural signal, so reveal its shell before the readouts.
                if (nexusBridge) {
                    transitionTimeline
                        .fromTo(
                            nexusBridge,
                            {
                                opacity: 0.25,
                                rotateX: 8,
                                scale: 0.88,
                                transformOrigin: '50% 50%',
                            },
                            {
                                opacity: 1,
                                rotateX: 0,
                                scale: 1,
                                ease: 'none',
                            },
                            0.5,
                        )
                        .fromTo(
                            '.home-nexus__bridge-line, .home-nexus__bridge-beacon, .home-nexus__bridge-readout',
                            { opacity: 0 },
                            { opacity: 1, ease: 'none', stagger: 0.08 },
                            0.64,
                        )
                }

                const gatewaySection = scope.current?.querySelector<HTMLElement>(
                    '.home-gateway',
                )
                const gatewayCards = scope.current?.querySelectorAll<HTMLElement>(
                    '.home-gateway__card',
                )
                let gatewayTimeline: gsap.core.Timeline | undefined

                // Cards enter after the section becomes readable, preserving the visual scan order.
                if (gatewaySection && gatewayCards && gatewayCards.length > 0) {
                    gatewayTimeline = gsap.timeline({
                        scrollTrigger: {
                            trigger: gatewaySection,
                            start: 'top 78%',
                            end: 'top 28%',
                            scrub: 0.6,
                            invalidateOnRefresh: true,
                        },
                    })

                    gatewayTimeline
                        .fromTo(
                            '.home-gateway__content',
                            { opacity: 0, y: 42 },
                            { opacity: 1, y: 0, ease: 'none' },
                            0,
                        )
                        .fromTo(
                            gatewayCards,
                            { opacity: 0, y: 28 },
                            {
                                opacity: 1,
                                y: 0,
                                ease: 'none',
                                stagger: 0.12,
                            },
                            0.12,
                        )
                }

                return () => {
                    heroMedia?.pause()
                    transitionTimeline.scrollTrigger?.kill()
                    transitionTimeline.kill()
                    gatewayTimeline?.scrollTrigger?.kill()
                    gatewayTimeline?.kill()
                }
            })

            return () => media.revert()
        },
        { scope },
    )
}
