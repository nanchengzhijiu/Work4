package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("follow")
public class Follow {
    private String id;
    private String userId;
    private String toUserId;
}
