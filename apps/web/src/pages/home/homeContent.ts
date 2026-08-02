export type GatewayEntryId = 'projects' | 'blog' | 'agent'

interface GatewayEntryBase {
    id: GatewayEntryId
    label: string
    title: string
    description: string
}

/**
 * 使用可辨识联合表达入口状态，防止“构建中”项目被误渲染成伪链接。
 *
 * available：必须提供真实地址。
 * building：禁止提供地址，只展示不可用状态。
 */
export type GatewayEntry = GatewayEntryBase & (
    | {
        status: 'available'
        statusLabel: string
        href: string
    }
    | {
        status: 'building'
        statusLabel: string
        href?: never
    }
)

export interface HomeContent {
    hero: {
        sectionLabel: string
        title: string
        description: string
        scrollCue: string
    }
    transition: {
        sectionLabel: string
        title: string
        description: string
    }
    gateway: {
        sectionLabel: string
        title: string
        description: string
        entries: readonly GatewayEntry[]
    }
}

/**
 * 首页稳定内容的唯一来源。
 *
 * 这里只保存文案和业务状态，不保存 DOM 引用、动画参数或角色坐标，
 * 以免内容配置与具体视觉实现相互耦合。
 */
export const homeContent = {
    hero: {
        sectionLabel: 'PERSONAL CREATIVE SPACE',
        title: 'Purple Nexus',
        description: '一个关于视觉、交互与智能体验的个人实验空间',
        scrollCue: '继续浏览'
    },
    transition: {
        sectionLabel: 'VISUAL EXPERIENCE',
        title: '视觉与交互, 在这里相遇',
        description: '通过连续的页面变化, 呈现 Purple Nexus 的主题体验'
    },
    gateway: {
        sectionLabel: 'EXPLORE',
        title: '探索更多内容',
        description: '项目、文章与智能体将陆续开放',
        entries: [
            {
                id: 'projects',
                label: 'SELECTED WORK',
                title: 'Projects',
                description: '收录个人项目、界面作品与视觉实验',
                status: 'building',
                statusLabel: '建设中'
            },
            {
                id: 'blog',
                label: 'WRITING',
                title: 'Blog',
                description: '记录开发过程、设计思考与总结实践',
                status: 'building',
                statusLabel: '建设中'
            },
            {
                id: 'agent',
                label: 'AI EXPERIENCE',
                title: 'Agent',
                description: '探索知识检索、长期记忆与工具调用的智能体验',
                status: 'building',
                statusLabel: '建设中'
            }
        ]
    }
} as const satisfies HomeContent