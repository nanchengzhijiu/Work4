package work.work4.dto;

import lombok.Data;

@Data
public class FollowDto {
    private Long userId;
    private Long followId;
    private int actionType;
}
