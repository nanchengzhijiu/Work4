package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("like")
public class Like {
    private Long id;
    private Long videoId;
    private Integer total;
    private Long commentId;
    private Long userId;
    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createdAt;
}
