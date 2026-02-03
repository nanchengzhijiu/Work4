package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("like")
public class Like {
    private Long id;
    private Long videoId;
    private Integer total;
    private Long commentId;
    private Long userId;
}
