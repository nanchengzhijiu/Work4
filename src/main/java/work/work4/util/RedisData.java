package work.work4.util;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RedisData {
    private LocalDateTime ExpireTime;
    private Object data;
}
