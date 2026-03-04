package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
@Data
@Accessors(chain = true)
@TableName("search")
public class Search {
    private String id;
    private String keyword;
    private LocalDateTime publishTime;
    private String type;
    private String year;
}
