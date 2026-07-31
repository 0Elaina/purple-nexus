import { HomeFooter } from "./components/HomeFooter";
import { HomeHeader } from "./components/HomeHeader";

/**
 * Purple Nexus 首页的页面级组件
 */
export function HomePage() {
    return (
        <>
            <HomeHeader />
            <main
                id="main-content"
                tabIndex={-1}
                className="grid min-h-svh place-items-center px-6 py-24"
            >
                <h1 className="font-display text-4xl font-semibold tracking-wide text-primary">
                    Purple Nexus
                </h1>
            </main>
            <HomeFooter />
        </>
    )
}