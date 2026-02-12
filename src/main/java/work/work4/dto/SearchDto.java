package work.work4.dto;

import lombok.Data;

@Data
public class SearchDto {
    private String keyword;
    private Integer pageSize;
    private Integer pageNum;
    private String username;
}
