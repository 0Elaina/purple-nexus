package com.purple.project.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.purple.project.read.ProjectDetail;
import com.purple.project.read.ProjectListItem;
import com.purple.project.service.ProjectQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 暴露访客可用的作品公开读取接口。
 *
 * Controller 只负责 HTTP 路由，不直接访问 Mapper。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@Slf4j
@Tag(name = "作品", description = "作品相关接口")
public class PublicProjectController {
    private final ProjectQueryService projectQueryService;

    /**
     * 返回全部已发布作品。
     *
     * @return 已发布作品；没有公开作品时响应 200 和空数组
     */
    @GetMapping
    @Operation(summary = "获取全部已发布作品")
    public List<ProjectListItem> findAllPublished() {
        List<ProjectListItem> projects = projectQueryService.findAllPublished();
        log.debug("公开作品列表读取完成, count={}", projects.size());
        return projects;
    }

    /**
     * 按 slug 返回已发布作品详情。
     *
     * @param slug 作品公开标识
     * @return 已发布作品详情
     */
    @GetMapping("/{slug}")
    @Operation(summary = "根据公开的slug标识获取作品")
    public ProjectDetail findPublishedBySlug(
            @PathVariable("slug") String slug) {
        ProjectDetail project = projectQueryService.findPublishedBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        log.debug("公开作品详情读取完成, slug={}", slug);
        return project;
    }
}
