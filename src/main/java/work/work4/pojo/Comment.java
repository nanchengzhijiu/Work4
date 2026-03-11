package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("comment")
public class Comment {
    private String id;
    private String userId;
    private String videoId;
    private String parentId;
    private Integer likeCount;
    private Integer childCount;
    private String content;
    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill= FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
