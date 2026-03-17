package work.work4.util;

public class UserContext {
    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void remove() {
        USER_ID_HOLDER.remove(); // 必须手动清理，防止内存泄漏（尤其是线程池环境）
    }
}
