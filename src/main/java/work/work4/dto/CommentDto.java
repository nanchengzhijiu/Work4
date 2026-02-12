package work.work4.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Long videoId;
    private Long commentId;
    private String cotent;
}
