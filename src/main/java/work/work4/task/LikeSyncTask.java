package work.work4.task;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import work.work4.mapper.VideoMapper;

import java.util.Set;


@Component
public class LikeSyncTask {

    @Resource
    private RedisTemplate<String, Object> template;
    @Resource
    private VideoMapper videoMapper;
    private static final String VIDEO_LIKE_COUNT_KEY = "video:like:count:";
    // 每 5 分钟同步一次视频点赞数
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncVideoLikeCount() {
        String dirtySetKey = "sync:video:ids";
        // 1. 获取所有发生过变动的视频 ID
        Set<Object> videoIds = template.opsForSet().members(dirtySetKey);
        if (CollectionUtils.isEmpty(videoIds)) return;

        for (Object idObj : videoIds) {
            String videoId = String.valueOf(idObj);
            String countKey = VIDEO_LIKE_COUNT_KEY + videoId;

            // 2. 从 Redis 获取最新值
            Object count = template.opsForValue().get(countKey);
            if (count != null) {
                // 3. 更新数据库
                videoMapper.updateLikeCount(videoId, Integer.parseInt(count.toString()));
                // 4. 同步完后从脏集合中移除
                template.opsForSet().remove(dirtySetKey, videoId);
            }
        }
    }
}
