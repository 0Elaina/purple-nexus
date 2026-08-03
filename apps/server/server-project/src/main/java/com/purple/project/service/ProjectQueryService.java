package com.purple.project.service;

import java.util.List;
import java.util.Optional;

import com.purple.project.read.ProjectDetail;
import com.purple.project.read.ProjectListItem;

/**
 * 提供作品公开读取用例。
 *
 * 发布过滤由 Mapper 查询强制执行，本服务不允许调用方改变公开边界。
 */
public interface ProjectQueryService {
    /**
     * 获取所有已发布作品列表
     *
     * @return 已发布作品
     */
    List<ProjectListItem> findAllPublished();

    /**
     * 按公开 slug 读取已发布作品。
     *
     * @param slug 作品公开标识
     * @return 已发布作品详情；不存在或未发布时为空
     */
    Optional<ProjectDetail> findPublishedBySlug(String slug);
}
