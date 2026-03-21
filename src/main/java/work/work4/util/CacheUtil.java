package work.work4.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import work.work4.vo.VideoVo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static work.work4.common.RedisConstants.SEARCH_CACHE_KEY;

@Component
public class CacheUtil {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    public void set(String key, Object value, Long Time, TimeUnit unit) {
        String strValue;
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            // 如果是字符序列、数字或布尔值，直接转 String，不走 JSON 序列化
            strValue = String.valueOf(value);
        } else {
            // 如果是对象、集合、Map等，走 JSON 序列化
            strValue = JSONUtil.toJsonStr(value);
        }
        stringRedisTemplate.opsForValue().set(key, strValue, Time, unit);
    }
    public void setWithLogicalExpire(String key, Object value, Long Time, TimeUnit unit) {
        String strValue;
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            // 如果是字符序列、数字或布尔值，直接转 String，不走 JSON 序列化
            strValue = String.valueOf(value);
        } else {
            // 如果是对象、集合、Map等，走 JSON 序列化
            strValue = JSONUtil.toJsonStr(value);
        }
//        设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(strValue);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(Time)));
        stringRedisTemplate.opsForValue().set(key, strValue, Time, unit);
    }
    public <R,E> R queryWithPassThrough(
            String redisKey,
            Long time, TimeUnit unit,
            Class<R> type,
            Class<E> elementType,
            Supplier<R> dbFallback) {

        // 1. 从 Redis 查询
        String json = stringRedisTemplate.opsForValue().get(redisKey);

        // 2. 判断是否存在且不为空（StrUtil.isNotBlank 过滤了 null 和 ""）
        if (StrUtil.isNotBlank(json)) {
            // 如果提供了 elementType，说明期望返回的是 List<E>
            if (elementType != null) {
                return (R) JSONUtil.toList(json, elementType);
            }
            // 否则返回普通对象 R
            return JSONUtil.toBean(json, type);
        }

        // 3. 命中空值缓存（解决缓存穿透）：json 不为 null 说明是 ""
        if (json != null) {
            return null;
        }

        // 4. 缓存没命中，查数据库
        R r = dbFallback.get();

        // 5. 判断数据库结果
        if (r == null || (r instanceof Collection && ((Collection<?>) r).isEmpty())) {
            // 数据库也没有，缓存空字符串（过期时间设短一点，如 2 分钟）
            stringRedisTemplate.opsForValue().set(redisKey, "", 2, TimeUnit.MINUTES);
            return null;
        }

        // 6. 数据库有值，写入缓存
        this.set(redisKey, r, time, unit);
        return r;
    }
    public <R,E> R queryWithLogicalExpire(String redisKey, String keywords, Integer pageSize, Integer pageNum, String username,
                                         Long time, TimeUnit unit,
                                         Class<R> type, Class<E> elementType,
                                         Supplier<R> dbFallback) {
        // 1. 尝试从缓存获取
        String json = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isBlank(json))
            return null;
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r;
        LocalDateTime expireTime = redisData.getExpireTime();
        Object data = redisData.getData();
        if (elementType != null) {
            // 如果是列表，将 Object 形式的 data 转回 List<E>
            r = (R) JSONUtil.toList(JSONUtil.parseArray(data), elementType);
        } else {
            // 如果是对象
            r = JSONUtil.toBean((JSONObject) data, type);
        }

        // 4. 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回旧数据
            return r;
        }
        // 4. 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回旧数据
            return r;
        }
        // 5. 已过期，尝试获取互斥锁进行异步重建
        String lockKey = "lock:rebuild:" + keywords;
        if (tryGetLock(lockKey)) {
            // 拿到锁后再次检查（Double Check），防止重复开启线程
            String doubleCheckJson = stringRedisTemplate.opsForValue().get(redisKey);
            RedisData doubleCheckData = JSONUtil.toBean(doubleCheckJson, RedisData.class);
            if (doubleCheckData.getExpireTime().isAfter(LocalDateTime.now())) {
                releaseLock(lockKey); // 如果已经有人更新了，直接放锁走人
                return r;
            }

            // 6. 开启独立线程异步写入 Redis
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R newR = dbFallback.get();
                    // 封装逻辑过期数据并写入（注意：这里的 set 逻辑需要封装 RedisData）
                    this.setWithLogicalExpire(redisKey, newR, time, unit);
                } catch (Exception e) {
                    System.out.println("缓存重建失败: {}"+e.getMessage());
                } finally {
                    // 释放锁
                    releaseLock(lockKey);
                }
            });
        }
        // 7. 没拿到锁的线程，直接返回过期的旧数据
        return r;
    }
    public boolean tryGetLock(String key) {
        Boolean flag=stringRedisTemplate.opsForValue().setIfAbsent(key,"1",1, TimeUnit.HOURS);
        return BooleanUtil.isTrue(flag);
    }
    public void releaseLock(String key) {

        stringRedisTemplate.delete(key);
    }
}

