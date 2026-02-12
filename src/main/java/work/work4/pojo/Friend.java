package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("friends")
public class Friend {
    private Long id;
    private Long userId;
    private Long friendId;
}
