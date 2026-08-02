import type { HomeContent } from "../homeContent";

interface NexusTransitionSceneProps {
    content: HomeContent['transition']
}

/**
 * 首页中段的主题转换场景。
 *
 * 负责呈现深紫色静态环境、场景标题和 Nexus 核心；
 * 不在组件内部注册滚动监听或管理动画生命周期。
 */
export function NexusTransitionScene(
    { content }: NexusTransitionSceneProps
) {
    return (
        <section
            id="nexus-transition"
            aria-labelledby="nexus-title"
            className="home-nexus relative isolate grid min-h-svh overflow-hidden px-6 py-24 text-on-primary sm:px-8"
        >
            <div className="home-nexus__content relative z-10 mx-auto grid w-full max-w-7xl items-center gap-16 lg:grid-cols-2">
                <div className="max-w-xl">
                    <p className="font-display text-xs font-semibold tracking-widest text-on-primary opacity-75">
                        {content.sectionLabel}
                    </p>

                    <h2
                        id="nexus-title"
                        className="mt-4 font-display text-4xl font-semibold tracking-wide sm:text-6xl"
                    >
                        {content.title}
                    </h2>

                    <p className="mt-6 text-base leading-7 text-on-primary opacity-80 sm:text-lg">
                        {content.description}
                    </p>
                </div>

                <div
                    aria-hidden="true"
                    className="home-nexus__bridge relative mx-auto"
                >
                    <div className="home-nexus__bridge-grid" />
                    <div className="home-nexus__bridge-line home-nexus__bridge-line--top" />
                    <div className="home-nexus__bridge-line home-nexus__bridge-line--bottom" />
                    <div className="home-nexus__bridge-beacon">
                        <span className="home-nexus__bridge-beacon-core" />
                    </div>
                    <div className="home-nexus__bridge-readout">
                        <span>SYNC BRIDGE</span>
                        <span>02 / 03</span>
                    </div>
                </div>
            </div>

        </section>
    )
}
