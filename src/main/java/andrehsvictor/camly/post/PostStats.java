package andrehsvictor.camly.post;

import java.time.LocalDateTime;

public interface PostStats {
    Long getTotalPosts();

    Long getTotalLikes();

    Double getAverageLikes();

    Long getMaxLikes();

    Long getMinLikes();

    Double getEngagementRate();

    LocalDateTime getLastPostDate();
}