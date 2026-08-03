package com.purple.project.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.purple.project.mapper.ProjectMapper;
import com.purple.project.read.ProjectDetail;
import com.purple.project.read.ProjectListItem;
import com.purple.project.service.ProjectQueryService;

import lombok.RequiredArgsConstructor;

/**
 * 作品查询服务实现
 */
@Service
@RequiredArgsConstructor
public class ProjectQueryServiceImpl implements ProjectQueryService {
    private final ProjectMapper projectMapper;

    /**
     * 读取全部已发布作品。
     *
     * @return 已发布作品；没有公开作品时返回空列表
     */
    @Override
    public List<ProjectListItem> findAllPublished() {
        return projectMapper.findAllPublished();
    }

    /**
     * 按公开 slug 读取已发布作品。
     *
     * @param slug 作品公开标识
     * @return 已发布作品详情；不存在或未发布时为空
     */
    @Override
    public Optional<ProjectDetail> findPublishedBySlug(String slug) {
        return projectMapper.findPublishedBySlug(slug);
    }
}
