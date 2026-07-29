import { describe, expect, it } from 'vitest'

import { loadRuntimeConfig } from './runtimeConfig'

describe('loadRuntimeConfig', () => {
    /**
     * 验证合法配置能够被读取，并统一清理首尾空格和末尾斜杠。
     */
    it('returns normalized configuration for a valid API URL', () => {
        // Given：模拟开发环境中提供的公开 API 地址。
        const environment = {
            VITE_API_BASE_URL: '  http://localhost:8080/  '
        }

        // When：读取并校验运行配置。
        const config = loadRuntimeConfig(environment)

        // Then：业务代码获得格式统一的 API 基地址。
        expect(config).toEqual({
            apiBaseUrl: 'http://localhost:8080'
        })
    })

    /**
     * 验证必需配置缺失时，应用不会带着错误配置继续启动。
     */
    it('throws a clear error when the API URL is missing', () => {
        // Given：模拟没有提供 VITE_API_BASE_URL 的环境。
        const environment = {}

        // When / Then：读取配置时应立即指出缺失的变量名称。
        expect(() => loadRuntimeConfig(environment))
            .toThrow('缺少 VITE_API_BASE_URL 运行时配置')
    })
})