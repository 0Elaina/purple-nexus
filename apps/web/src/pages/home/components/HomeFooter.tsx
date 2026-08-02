/**
 * 首页私有的最小页脚。
 *
 * 仅承载项目性质和素材权利声明，不为尚未存在的页面创建站点地图。
 */
export function HomeFooter() {
    return (
        <footer className="home-footer px-4 pb-8 sm:px-6">
            <div className="home-footer__inner mx-auto max-w-7xl border-t px-2 pt-6">
                <p className="max-w-2xl text-sm leading-6">
                    Purple Nexus 是个人非商业、非官方的视觉与技术实验项目。
                    相关角色与素材版权归原权利方所有。
                </p>
            </div>
        </footer>
    )
}
