import type { GatewayEntry, HomeContent } from "../homeContent";

interface GatewaySceneProps {
    content: HomeContent['gateway']
}

interface GatewayEntryCardProps {
    entry: GatewayEntry
    featured?: boolean
}

const gatewayEntryClassName =
    "home-gateway__card flex min-h-64 flex-col rounded-large p-6"

/**
 * 根据开放状态渲染首页入口。
 *
 * available 使用真实链接；building 只展示内容和状态，
 * 避免尚未开放的功能进入键盘焦点顺序。
 */
function GatewayEntryCard({ entry, featured = false }: GatewayEntryCardProps) {
    const titleId = `gateway-${entry.id}-title`
    const cardClassName = `${gatewayEntryClassName} ${
        featured ? 'home-gateway__card--featured' : ''
    }`

    const body = (
        <>
            <p className="home-gateway__card-label font-display text-xs font-semibold tracking-[0.2em]">
                {entry.label}
            </p>
            <h3
                id={titleId}
                className="mt-4 font-display text-2xl font-semibold text-white sm:text-3xl"
            >
                {entry.title}
            </h3>
            <p className="mt-4 max-w-xl leading-7 text-slate-300">
                {entry.description}
            </p>
            <p className="mt-auto pt-8">
                <span
                    className={`home-gateway__status ${
                        entry.status === 'building'
                            ? 'home-gateway__status--building'
                            : 'home-gateway__status--available'
                    }`}
                >
                    {entry.statusLabel}
                </span>
            </p>
        </>
    )

    // 如果路由实体的状态是 available
    if (entry.status === 'available') {
        // 返回目标超链接
        return (
            <a
                href={entry.href}
                aria-labelledby={titleId}
                className={`${cardClassName} transition-transform`}
            >
                {body}
            </a>
        )
    }

    // 建设中
    return (
        <article
            aria-labelledby={titleId}
            className={cardClassName}
        >
            {body}
        </article>
    )
}

/**
 * 首页第三段功能入口场景。
 *
 * 负责呈现 Projects、Blog 和 Agent 的内容及真实开放状态；
 * 不负责路由配置或远程数据读取。
 */
export function GatewayScene({ content }: GatewaySceneProps) {
    return (
        <section
            id="gateway"
            aria-labelledby="gateway-title"
            className="home-gateway relative grid min-h-svh place-items-center overflow-hidden px-6 py-24 sm:px-8"
        >
            <div className="home-gateway__content relative z-10 mx-auto w-full max-w-7xl">
                <div className="max-w-2xl">
                    <p className="font-display text-xs font-semibold tracking-[0.24em] text-cyan-100">
                        {content.sectionLabel}
                    </p>

                    <h2
                        id="gateway-title"
                        className="mt-4 font-display text-4xl font-semibold tracking-wide text-white sm:text-6xl"
                    >
                        {content.title}
                    </h2>

                    <p className="mt-6 text-base leading-7 text-slate-300 sm:text-lg">
                        {content.description}
                    </p>
                </div>

                <div className="home-gateway__grid mt-12">
                    {content.entries.map((entry, index) => (
                        <GatewayEntryCard
                            key={entry.id}
                            entry={entry}
                            featured={index === 0}
                        />
                    ))}
                </div>
            </div>
        </section>
    )
}
