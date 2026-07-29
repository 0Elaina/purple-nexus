/**
 * 浏览器运行时允许读取的公开环境变量。
 *
 * 这里只声明 VITE_API_BASE_URL，避免后续代码随意读取其他环境变量。
 * VITE_ 开头的值会进入浏览器构建产物，因此绝对不能放入密码或 Token。
 */
interface PublicRuntimeEnvironment {
    readonly VITE_API_BASE_URL?: string
}

/**
 * Web 应用内部使用的运行配置。
 *
 * 业务代码以后只依赖这个对象，不直接依赖 Vite 的 import.meta.env。
 */
export interface RuntimeConfig {
    readonly apiBaseUrl: string
}

/**
 * 允许的后端 API 协议白名单集合。
 *
 * 校验 apiBaseUrl 合法性时仅允许 http: 和 https: 协议，
 * 避免配置非法的 URI 方案（如 javascript:、file: 或 data:）。
 */
const SUPPORTED_API_PROTOCOLS = new Set(['http:', 'https:'])

/**
 * 读取并校验 Web 运行配置。
 *
 * @param environment - 默认读取 Vite 提供的 import.meta.env，测试时可传入独立对象。
 * @returns 经过校验和标准化的 API 基地址。
 */
export function loadRuntimeConfig(
    environment: PublicRuntimeEnvironment = {
        VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL
    }
): RuntimeConfig {
    const rawApiBaseUrl = environment.VITE_API_BASE_URL?.trim()

    if (!rawApiBaseUrl) {
        throw new Error("缺少 VITE_API_BASE_URL 运行时配置")
    }

    let parsedApiBaseUrl: URL

    try {
        parsedApiBaseUrl = new URL(rawApiBaseUrl)
    } catch {
        throw new Error(`VITE_API_BASE_URL 配置错误，${rawApiBaseUrl} 不是合法的绝对 URL`)
    }

    // 确保协议是 http: 或 https:
    if (!SUPPORTED_API_PROTOCOLS.has(parsedApiBaseUrl.protocol)) {
        throw new Error(`VITE_API_BASE_URL 协议必须是 http: 或 https:，实际：${parsedApiBaseUrl.protocol}`)
    }

    return {
        // 统一移除末尾斜杠，避免以后拼接 "/api/..." 时产生双斜杠
        apiBaseUrl: parsedApiBaseUrl.toString().replace(/\/$/, '')
    }
}