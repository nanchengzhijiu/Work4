package work.work4.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import work.work4.mapper.LikeMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Like;

import java.util.List;
import java.util.Map;
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
        // 1. 获取队列中的原始 Object 列表
        List<Object> actions = template.opsForList().range(queueKey, 0, -1);
        if (CollectionUtils.isEmpty(actions)) return;
        // 清理已读取的缓存
        template.opsForList().trim(queueKey, actions.size(), -1);

        for (Object action : actions) {
            try {
                // 【关键点】此时 action 是一个 Map (Fastjson 默认解析结果)
                Map<String, Object> map = (Map<String, Object>) action;
                String userId = String.valueOf(map.get("userId"));
                String videoId = (String) map.get("videoId");
                String commentId = (String) map.get("commentId");
                String type = String.valueOf(map.get("actionType")); // "1"点赞 "0"取消
                // 判断是视频还是评论
                boolean isVideo = !StringUtils.isEmpty(videoId);
                if ("1".equals(type)) {
                    Like like = new Like().setUserId(userId)
                            .setVideoId(videoId)
                            .setCommentId(commentId);
                    // 使用 insertIgnore 或 try-catch 处理唯一索引冲突
                    try {
                        likeMapper.insert(like);
                    } catch (Exception ignored) {
                        System.out.println("出错啦"+ignored);
                    }
                } else {
                    // 执行删除逻辑
                    LambdaQueryWrapper<Like> qw = new LambdaQueryWrapper<Like>()
                            .eq(Like::getUserId, userId)
                            .eq(isVideo, Like::getVideoId, videoId)
                            .eq(!isVideo, Like::getCommentId, commentId);
                    likeMapper.delete(qw);
                }
            } catch (Exception e) {
            }
        }
    }
}
