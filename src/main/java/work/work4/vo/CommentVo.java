package work.work4.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CommentVo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("video_id")
    private String videoId;
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("like_count")
    private Integer likeCount;
    @JsonProperty("child_count")
    private Integer childCount;
    @JsonProperty("content")
    private String content;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 确保时间格式一致
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonProperty("deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;
}
