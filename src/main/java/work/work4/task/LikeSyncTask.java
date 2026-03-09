package work.work4.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import work.work4.mapper.LikeMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Like;

import java.util.List;
import java.util.Set;


@Component
public class LikeSyncTask {

    @Resource
    private RedisTemplate<String, Object> template;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private LikeMapper likeMapper;
    private static final String VIDEO_LIKE_COUNT_KEY = "video:like:count:";
    // 每 5 分钟同步一次视频点赞数
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncVideoLikeCount() {
        String dirtySetKey = "sync:video:ids";
        String actionQueueKey = "queue:like:actions";
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
    /**
     * 每 30 秒同步一次点赞行为 (Like 记录表)
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncLikeRecords() {
        String queueKey = "queue:like:actions";
        // 批量取出当前队列中所有消息
        List<Object> actions = template.opsForList().range(queueKey, 0, -1);
        if (CollectionUtils.isEmpty(actions)) return;

        // 立即截断队列，防止重复消费
        template.opsForList().trim(queueKey, actions.size(), -1);

        for (Object action : actions) {
            try {
                String[] parts = String.valueOf(action).split(":");
                String userId = parts[0];
                String targetId = parts[1];
                String type = parts[2];
                boolean isVideo = Boolean.parseBoolean(parts[3]);
                if ("1".equals(type)) {
                    Like like = new Like().setUserId(userId);
                    if (isVideo) like.setVideoId(targetId); else like.setCommentId(targetId);
                    // 唯一索引冲突说明已点过，直接忽略
                    likeMapper.insert(like);
                } else {
                    LambdaQueryWrapper<Like> qw = new LambdaQueryWrapper<Like>()
                            .eq(Like::getUserId, userId)
                            .eq(isVideo, Like::getVideoId, targetId)
                            .eq(!isVideo, Like::getCommentId, targetId);
                    likeMapper.delete(qw);
                }
            } catch (Exception e) {
            }
        }
    }
}
