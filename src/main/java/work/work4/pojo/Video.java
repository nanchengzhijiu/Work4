package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalTime;

@Data
@TableName("video")
public class Video {
    private Long id;
    private Long userId;
    private String title;
    private String videoUrl;
    private String coverUrl;
    private String data;
    private LocalTime latestTime;
    private String description;
    private Integer visitCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalTime createdAt;
    private LocalTime updatedAt;
    private LocalTime deletedAt;
}
