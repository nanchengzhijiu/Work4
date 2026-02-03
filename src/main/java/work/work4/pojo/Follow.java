package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("follow")
public class Follow {
    private Long id;
    private Long userId;
    private Long followId;
}
