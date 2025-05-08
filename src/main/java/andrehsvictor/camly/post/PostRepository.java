package andrehsvictor.camly.post;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByOrderByEngagementRateDescAndCreatedAtBetween(
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
            SUM(p.likeCount) AS totalLikes,
            SUM(p.commentCount) AS totalComments,
            AVG(p.likeCount) AS averageLikes,
            AVG(p.commentCount) AS averageComments
            FROM Post p
            WHERE p.user.id = :userId
                """)
    PostStats getPostStatsByUserId(UUID userId);

}

interface PostStats {

    Long getPostCount();

    Long getTotalLikes();

    Long getTotalComments();

    Double getAverageLikes();

    Double getAverageComments();

}