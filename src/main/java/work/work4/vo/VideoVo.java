package work.work4.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class VideoVo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("video_url")
    private String videoUrl;
    @JsonProperty("cover_url")
    private String coverUrl;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("visit_count")
    private Integer visitCount;
    @JsonProperty("like_count")
    private Integer likeCount;
    @JsonProperty("comment_count")
    private Integer commentCount;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
