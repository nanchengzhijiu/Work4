package work.work4.dto;

import lombok.Data;

@Data
public class FollowDto {
    private String userId;
    private String followId;
    private int actionType;
}
