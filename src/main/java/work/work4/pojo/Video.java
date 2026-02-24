package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("video")
public class Video {
    private Long id;
    private Long userId;
    private String title;
    private String videoUrl;
    private String coverUrl;
    private long during;
    private String description;
    private Integer visitCount;
    private Integer likeCount;
    private Integer commentCount;
    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill= FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    private String type;
    private String year;
}
