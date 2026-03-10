package work.work4.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LikeDto {
    private String userId;
    private String videoId;
    private String commentId;
    private String actionType;
}
