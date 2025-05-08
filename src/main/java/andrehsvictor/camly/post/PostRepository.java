package andrehsvictor.camly.post;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByCreatedAtBetweenOrderByEngagementRateDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    @Query("""
            SELECT p
            FROM Post p
            WHERE (LOWER(:query) IS NULL OR LOWER(p.user.username) LIKE LOWER(CONCAT('%', :query, '%')))
                AND (:username IS NULL OR p.user.username = :username)
            """)
    Page<Post> findAllWithFilters(
            String query,
            String username,
            Pageable pageable);

    Page<Post> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT COUNT(p.id) > 0 FROM Post p WHERE p.user.id = :userId AND p.id = :postId")
    boolean existsLikeByUserIdAndPostId(UUID userId, UUID postId);

    @Query("""
            SELECT
            COUNT(p.id) AS postCount,
            COALESCE(SUM(p.likeCount), 0) AS totalLikes,
            COALESCE(AVG(p.likeCount), 0) AS averageLikes,
            COALESCE(MAX(p.likeCount), 0) AS maxLikes,
            COALESCE(MIN(CASE WHEN p.likeCount > 0 THEN p.likeCount ELSE NULL END), 0) AS minLikes,
            COALESCE((SUM(p.likeCount) * 1.0) / NULLIF(COUNT(p.id), 0), 0) AS engagementRate,
            MAX(p.createdAt) AS lastPostDate
            FROM Post p
            WHERE p.user.id = :userId
            """)
    PostStats getPostStatsByUserId(UUID userId);

}

interface PostStats {
    Long getPostCount();
    Long getTotalLikes();
    Double getAverageLikes();
    Long getMaxLikes();
    Long getMinLikes();
    Double getEngagementRate();
    LocalDateTime getLastPostDate();
}