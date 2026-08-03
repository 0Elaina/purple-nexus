package com.purple.project.read;

import java.time.Instant;

/**
 * 面向公开作品详情的读取结果。
 *
 * 在列表展示字段基础上增加完整介绍；不承载写入状态、
 * 所有者信息或数据库审计字段。
 *
 * @param slug        作品公开地址使用的稳定标识
 * @param title       作品标题
 * @param summary     作品摘要
 * @param description 详情页aw完整介绍
 * @param coverUrl    可选封面地址；允许为空
 * @param publishedAt 公开发布时间
 */
public record ProjectDetail(
        String slug,
        String title,
        String summary,
        String description,
        String coverUrl,
        Instant publishedAt) {

}
