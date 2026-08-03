package com.purple.project.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.purple.project.read.ProjectDetail;
import com.purple.project.read.ProjectListItem;

/**
 * 作品公开读取的数据访问契约。
 *
 * SQL 由同名 XML Mapper 实现；接口不承担 HTTP 错误转换、
 * 发布状态修改或其他业务规则。
 */
@Mapper
public interface ProjectMapper {

    /**
     * 查询公开列表；没有已发布作品时返回空列表。
     * 
     * @return 已发布作品列表；没有公开作品时返回空列表
     */
    List<ProjectListItem> findAllPublished();

    /**
     * 查询公开详情；不存在和未发布作品统一返回空结果。
     * 
     * @param slug 公开 slug
     * @return 拥有公开属性的作品详情；未发布或不存在则为空
     */
    Optional<ProjectDetail> findPublishedBySlug(@Param("slug") String slug);
}
