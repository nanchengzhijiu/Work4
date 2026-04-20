package work.work4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ThreadPoolExecutor {

    @Bean(name = "videoUploadTaskExecutor")
    public Executor videoUploadTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // 核心线程数
        executor.setMaxPoolSize(10);       // 最大线程数
        executor.setQueueCapacity(100);    // 队列大小
        executor.setThreadNamePrefix("VideoUpload-");
        executor.initialize();
        return executor;
    }
    @Bean
    public ThreadPoolTaskExecutor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Set core thread count
        executor.setCorePoolSize(10);
        // Set max thread count
        executor.setMaxPoolSize(20);
        // Set queue capacity
        executor.setQueueCapacity(200);
        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("ws-push-");

        // Rejection policy: How to handle overflow
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
