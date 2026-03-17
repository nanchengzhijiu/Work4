package work.work4.common;

public class RedisConstants {
    public static final String LOGIN_TOKEN_KEY = "login:token:";//缓存池子
    public static final String VIDEO_LIKE_COUNT_KEY = "video:like:count:";
    public static final String COMMENT_LIKE_COUNT_KEY = "comment:like:count:";
    public static final String USER_LIKE_ZSET = "user:likes:";
    public static final String COMMENT_COMMENT_COUNT_KEY = "comment:childComment:count:";
    public static final String VIDEO_COMMENT_COUNT_KEY = "video:comment:count:";
    public static final String VIDEO_VISIT_RANKING = "video:ranking:visit";// 点击率排行榜
    public static final String VIDEO_POOL_KEY = "video:global:pool";//随机池
    public static final String VIDEO_CACHE_KEY = "video:cache:";//缓存池子
}
