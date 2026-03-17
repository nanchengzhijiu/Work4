package work.work4.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class VideoDto {
    private String id;
    private String username;
    private String userId;
    private String title;
    private String videoUrl;
    private String coverUrl;
    private String type;
    private String year;
}
