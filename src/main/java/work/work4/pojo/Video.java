package work.work4.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Video {
    private Integer id;
    private Integer userId;
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
