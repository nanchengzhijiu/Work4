package work.work4.task;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import work.work4.config.RabbitConfiguration;
import work.work4.mapper.MessageMapper;
import work.work4.pojo.Messages;

import java.util.concurrent.TimeUnit;

/**
 * 聊天消息消费者
 * 双写策略：MySQL 为主，Redis 为缓存层
 * 设计思路：
 * 1. 优先保证 MySQL 写入成功（数据持久化）
 * 2. Redis 写入失败不阻断流程（缓存失败降级）
 * 3. 提供异常处理和日志记录便于问题追踪
 */
@Slf4j
@Component
public class ChatMessageConsumer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MessageMapper messageMapper;

    private static final String CHAT_HISTORY_KEY = "chat:history:";
    private static final int MAX_HISTORY_SIZE = 500;
    private static final int REDIS_EXPIRE_DAYS = 7;

    @RabbitListener(queues = RabbitConfiguration.CHAT_QUEUE)
    public void handleChatMessage(Messages msgs) {
        if (msgs == null) {
            log.warn("收到空消息，忽略处理");
            return;
        }

        try {
            // 步骤 1：优先写入 MySQL（数据持久化，必须成功）
            saveToMySQL(msgs);
            log.debug("消息已保存到 MySQL，chatId={}, messageId={}", msgs.getChatId(), msgs.getId());

            // 步骤 2：异步写入 Redis（缓存层，失败不影响主流程）
            try {
                saveToRedis(msgs);
                log.debug("消息已保存到 Redis，chatId={}", msgs.getChatId());
            } catch (Exception e) {
                // Redis 写入失败只记录日志，不中断流程
                // 原因：下次查询时可以从 MySQL 重新加载到 Redis
                log.warn("消息保存到 Redis 失败，chatId={}，将在下次查询时重新缓存：{}",
                         msgs.getChatId(), e.getMessage());
            }

        } catch (Exception e) {
            // MySQL 写入失败，这是严重问题
            log.error("消息保存到 MySQL 失败，chatId={}，消息内容可能丢失：{}",
                     msgs.getChatId(), e.getMessage(), e);
            // 可选：这里可以发送告警或将消息写入死信队列
            throw new RuntimeException("消息持久化失败", e);
        }
    }

    /**
     * 存储到 Redis（缓存层）
     * 设计特点：
     * - 只保留最新 500 条消息，防止内存溢出
     * - 7 天过期时间，自动清理冷消息
     * - 支持渐进式加载（用户打开聊天窗口时读取）
     */
    private void saveToRedis(Messages msgs) {
        if (msgs.getChatId() == null) {
            log.debug("聊天 ID 为空，跳过 Redis 缓存");
            return;
        }

        String redisKey = CHAT_HISTORY_KEY + msgs.getChatId();
        String messageJson = JSON.toJSONString(msgs);

        try {
            // 1. 将最新消息追加到 List 的右侧（尾部）
            // 这样 List 中的消息顺序就和时间顺序一致：左边旧，右边新
            stringRedisTemplate.opsForList().rightPush(redisKey, messageJson);

            // 2. 裁剪 List，只保留最新的 MAX_HISTORY_SIZE 条记录
            // 防止长期活跃的群聊把 Redis 内存撑爆
            stringRedisTemplate.opsForList().trim(redisKey, -MAX_HISTORY_SIZE, -1);

            // 3. 设置过期时间
            // 如果一个群聊 7 天没人说话，就让它的缓存在 Redis 中过期清理掉
            // 下次用户点开这个冷门群聊时，再从 MySQL 查出来重新放回 Redis
            stringRedisTemplate.expire(redisKey, REDIS_EXPIRE_DAYS, TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("Redis 操作异常，chatId={}：", msgs.getChatId(), e);
            throw e; // 重新抛出异常，由上层处理
        }
    }

    /**
     * 存储到 MySQL（持久化层）
     * 必须成功，否则消息丢失
     */
    private void saveToMySQL(Messages msgs) {
        try {
            messageMapper.insert(msgs);
        } catch (Exception e) {
            log.error("MySQL 插入失败，消息 ID={}，chatId={}：", msgs.getId(), msgs.getChatId(), e);
            throw e; // 重新抛出异常，阻止消息被标记为已消费
        }
    }
}