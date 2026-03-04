package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("friends")
public class Friend {
    private String id;
    private String userId;
    private String friendId;
}
