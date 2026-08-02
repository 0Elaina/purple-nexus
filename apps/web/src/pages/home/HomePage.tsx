import { useRef } from "react";
import { GatewayScene } from "./components/GatewayScene";
import { HeroScene } from "./components/HeroScene";
import { HomeFooter } from "./components/HomeFooter";
import { HomeHeader } from "./components/HomeHeader";
import { NexusTransitionScene } from "./components/NexusTransitionScene";
import { homeContent } from "./homeContent";
import { useHomeMotion } from "./hooks/useHomeMotion";
import "./styles/home.css";

/**
 * Purple Nexus 首页的页面级组件
 */
export function HomePage() {
    const pageRef = useRef<HTMLDivElement>(null)

    useHomeMotion(pageRef)

    return (
        <div ref={pageRef} className="home-page-shell">
            <HomeHeader />
            <main
                id="main-content"
                tabIndex={-1}
            >
                <HeroScene content={homeContent.hero} />
                <NexusTransitionScene content={homeContent.transition} />
                <GatewayScene content={homeContent.gateway} />
            </main>
            <HomeFooter />
        </div>
    )
}
