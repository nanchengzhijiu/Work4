package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    private Long id;
    private Long userId;
    private Long videoId;
    private Long commentId;
    private String content;
    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill= FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
