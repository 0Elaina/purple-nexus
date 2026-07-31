/**
 * 首页私有的最小页脚。
 *
 * 仅承载项目性质和素材权利声明，不为尚未存在的页面创建站点地图。
 */
export function HomeFooter() {
    return (
        <footer className="px-4 pb-6 sm:px-6">
            <div className="mx-auto max-w-7xl border-t border-border px-2 pt-6">
                <p className="text-sm leading-6 text-text-secondary">
                    Purple Nexus 是个人非商业、非官方的视觉与技术实验项目。
                    相关角色与素材版权归原权利方所有。
                </p>
            </div>
        </footer>
    )
}