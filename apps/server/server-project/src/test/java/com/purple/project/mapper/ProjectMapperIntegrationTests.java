package com.purple.project.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.purple.project.read.ProjectListItem;

/**
 * 使用真实 PostgreSQL 验证作品公开查询的发布隔离。
 *
 * 测试数据位于测试事务中，结束后统一回滚，不写入生产迁移。
 */
@Transactional
@SpringBootTest(
        classes = ProjectMapperIntegrationTests.TestApplication.class,
        properties = {
                "spring.config.import=optional:file:../.env[.properties]",
                "spring.datasource.url=${SPRING_DATASOURCE_URL}",
                "spring.datasource.username=${SPRING_DATASOURCE_USERNAME}",
                "spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}",
                "mybatis.configuration.map-underscore-to-camel-case=true",
                "mybatis.configuration.arg-name-based-constructor-auto-mapping=true"
        })
class ProjectMapperIntegrationTests {

    private static final String PUBLISHED_SLUG = "d3-01-test-published";
    private static final String DRAFT_SLUG = "d3-01-test-draft";
    private static final String FIRST_INSERTED_SLUG = "d3-01-test-order-first";
    private static final String SECOND_INSERTED_SLUG = "d3-01-test-order-second";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 证明草稿即使真实存在于数据库，也无法通过公开列表或详情查询读取。
     */
    @Test
    void excludesDraftFromPublicListAndDetail() {
        // Given：数据库同时存在已发布作品和草稿。
        insertPublishedProject(
                PUBLISHED_SLUG,
                OffsetDateTime.parse("2026-08-03T00:00:00Z"));

        jdbcTemplate.update("""
                INSERT INTO projects (
                    slug, title, summary, description, status
                )
                VALUES (?, ?, ?, ?, 'DRAFT')
                """,
                DRAFT_SLUG,
                "草稿测试作品",
                "草稿摘要",
                "草稿详情");

        // When：通过公开 Mapper 查询列表和详情。
        var publicSlugs = projectMapper.findAllPublished().stream()
                .map(ProjectListItem::slug)
                .toList();

        // Then：已发布作品可见，草稿在列表和详情中都不可见。
        assertThat(publicSlugs)
                .contains(PUBLISHED_SLUG)
                .doesNotContain(DRAFT_SLUG);

        assertThat(projectMapper.findPublishedBySlug(PUBLISHED_SLUG))
                .isPresent();

        assertThat(projectMapper.findPublishedBySlug(DRAFT_SLUG))
                .isEmpty();
    }

    /**
     * 相同发布时间使用数据库内部 ID 倒序，保证公开列表顺序确定。
     */
    @Test
    void ordersSamePublishedTimeByDescendingId() {
        // Given：两个作品发布时间相同，第二条数据拥有更大的数据库 ID。
        OffsetDateTime samePublishedAt =
                OffsetDateTime.parse("2026-08-03T01:00:00Z");

        insertPublishedProject(FIRST_INSERTED_SLUG, samePublishedAt);
        insertPublishedProject(SECOND_INSERTED_SLUG, samePublishedAt);

        // When：执行公开列表查询。
        var publicSlugs = projectMapper.findAllPublished().stream()
                .map(ProjectListItem::slug)
                .toList();

        // Then：较大的 ID 必须排在较小的 ID 前面。
        assertThat(publicSlugs)
                .contains(FIRST_INSERTED_SLUG, SECOND_INSERTED_SLUG);

        int firstIndex = publicSlugs.indexOf(FIRST_INSERTED_SLUG);
        int secondIndex = publicSlugs.indexOf(SECOND_INSERTED_SLUG);

        assertThat(secondIndex).isLessThan(firstIndex);
    }

    /**
     * 插入测试所需的已发布作品，避免多个排序场景重复维护建数 SQL。
     */
    private void insertPublishedProject(
            String slug,
            OffsetDateTime publishedAt) {
        jdbcTemplate.update("""
                INSERT INTO projects (
                    slug, title, summary, description, status, published_at
                )
                VALUES (?, ?, ?, ?, 'PUBLISHED', ?)
                """,
                slug,
                "测试作品",
                "测试摘要",
                "测试详情",
                publishedAt);
    }

    /**
     * 仅为当前 Mapper 集成测试提供自动配置入口。
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
