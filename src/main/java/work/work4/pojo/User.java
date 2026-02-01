package work.work4.pojo;

import lombok.Data;

import java.time.LocalTime;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String avatarUrl;
    private LocalTime createAt;
    private LocalTime updateAt;
    private LocalTime deleteAt;
}