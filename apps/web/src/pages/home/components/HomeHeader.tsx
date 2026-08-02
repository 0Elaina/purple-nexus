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
                className="fixed top-4 left-4 z-50 -translate-y-20 rounded-small bg-primary-strong px-4 py-3 text-sm font-semibold text-on-primary transition-transform focus-visible:translate-y-0"
            >
                跳至主要内容
            </a>
            <header
                className="home-header pointer-events-none fixed inset-x-0 top-0 z-40 px-4 pt-4 sm:px-6"
            >
                <div
                    className="home-header__rail pointer-events-auto mx-auto flex max-w-7xl items-center justify-between"
                >
                    <a
                        href="/"
                        aria-current="page"
                        className="home-header__brand rounded-full px-4 py-2 font-display text-base font-semibold tracking-wide transition-colors"
                    >
                        Purple Nexus
                    </a>
                    <span className="home-header__status hidden font-display text-[0.65rem] font-semibold tracking-[0.22em] sm:inline-flex">
                        VISUAL SYSTEM / 01
                    </span>
                </div>
            </header>
        </>
    )
}
