package work.work4.entity;

import lombok.Data;

@Data
public class Search {
    private String keyword;
    private Integer pageSize;
    private Integer pageNum;
}
