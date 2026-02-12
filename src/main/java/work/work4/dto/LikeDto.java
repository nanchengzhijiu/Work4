package work.work4.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class LikeDto {
    private Long videoId;
    private Long commentId;
    private int actionType;
}
