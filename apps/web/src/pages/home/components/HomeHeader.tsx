/**
 * 首页私有的顶部外壳。
 *
 * Header 只提供品牌入口，
 * 并允许键盘用户绕过顶部内容直接进入 main。
 */
export function HomeHeader() {
    return (
        <>
            <a
                href="#main-content"
                className="fixed top-4 left-4 z-50 -translate-y-20 rounded-small bg-primary-strong px-4 py-3 text-sm font-semibold text-on-primary transition-transform focus:translate-y-0"
            >
                跳至主要内容
            </a>
            <header
                className="sticky top-0 z-40 px-4 pt-4 sm:px-6"
            >
                <div
                    className="mx-auto flex max-w-7xl items-center rounded-large border border-border bg-surface-glass px-4 py-3 shadow-glass backdrop-blur-glass"
                >
                    <a
                        href="/"
                        className="font-display text-lg font-semibold tracking-wide text-primary-strong transition-colors hover:text-primary"
                    >
                        Purple Nexus
                    </a>
                </div>
            </header>
        </>
    )
}