import type { HomeContent } from "../homeContent";

interface HeroSceneProps {
    content: HomeContent['hero']
}

/**
 * 首页首屏场景。
 *
 * 负责将动态媒体与品牌内容组合成可独立理解的开场；
 * 视频只承担装饰性主视觉，加载或播放失败时仍保留完整标题与说明。
 */
export function HeroScene({ content }: HeroSceneProps) {
    return (
        <section
            aria-labelledby="hero-title"
            className="home-hero relative flex min-h-svh items-center overflow-hidden px-6 py-24 text-white sm:px-8"
        >
            <video
                className="home-hero__media pointer-events-none absolute inset-0 h-full w-full object-cover"
                loop
                muted
                playsInline
                preload="metadata"
                aria-hidden="true"
            >
                <source
                    src="/assets/home/hero-plana-loop.mp4"
                    type="video/mp4"
                />
            </video>
            <div
                className="home-hero__veil pointer-events-none absolute inset-0"
                aria-hidden="true"
            />
            <div
                className="home-hero__content relative z-10 mx-auto flex w-full max-w-7xl flex-col items-start text-left"
            >
                <p className="home-hero__eyebrow font-display text-xs font-semibold tracking-widest text-cyan-100">
                    {content.sectionLabel}
                </p>
                <h1
                    id="hero-title"
                    className="mt-4 font-display text-5xl font-semibold tracking-wide text-white sm:text-7xl lg:text-8xl"
                >
                    {content.title}
                </h1>

                <p className="mt-6 max-w-xl text-base leading-7 text-slate-200 sm:text-lg">
                    {content.description}
                </p>

                <p className="home-hero__scroll-cue mt-12 font-display text-xs tracking-widest text-cyan-100">
                    {content.scrollCue}
                </p>
            </div>
        </section>
    )
}
