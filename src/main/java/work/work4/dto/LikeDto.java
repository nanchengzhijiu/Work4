package work.work4.dto;

import lombok.Data;

@Data
public class LikeDto {
    private String videoId;
    private String commentId;
    private int actionType;
}
