package com.purple.project.read;

import java.time.Instant;

/**
 * 面向公开作品列表的轻量读取结果。
 *
 * 只承载卡片展示所需字段；不包含详情正文、内部数据库 ID 或发布状态，
 * 因为公开过滤由查询条件负责，而不是由调用方自行判断。
 *
 * @param slug        作品公开地址使用的稳定标识
 * @param title       作品标题
 * @param summary     列表卡片展示摘要
 * @param coverUrl    可选封面地址；缺失时页面仍可显示文本内容
 * @param publishedAt 公开发布时间，用于列表排序与展示
 */
public record ProjectListItem(
        String slug,
        String title,
        String summary,
        String coverUrl,
        Instant publishedAt) {

}
