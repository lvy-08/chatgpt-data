package cn.bug.chatgpt.data.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync//启用 Spring 的异步任务支持,如果项目中有异步方法（用 @Async 标注的方法），该注解会让它们运行在不同的线程中
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)//该注解是一个 POJO 类，用于从配置文件（如 application.yml）中读取线程池相关的配置信息
public class ThreadPoolConfig {

    @Bean//将返回的 ThreadPoolExecutor 对象注册为 Spring 容器中的 Bean
    @ConditionalOnMissingBean(ThreadPoolExecutor.class)// 如果 Spring 容器中尚未定义类型为 ThreadPoolExecutor 的 Bean，则创建这个 Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties properties) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // 实例化策略
        RejectedExecutionHandler handler;//拒绝策略： 当线程池的任务队列已满、且线程数已达最大值时，线程池会执行指定的拒绝策略。
        switch (properties.getPolicy()){
            case "AbortPolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();//（默认）： 抛出 RejectedExecutionException 异常
                break;
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();//直接丢弃新任务，不抛异常
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();//丢弃最早提交的任务，并尝试重新提交新任务。
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();//当前任务由调用线程（而不是线程池的工作线程）执行。
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }
        // 创建线程池
        return new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaxPoolSize(),//最大线程数
                properties.getKeepAliveTime(),//空闲线程存活时间
                TimeUnit.SECONDS,//// 时间单位
                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),// 阻塞队列
                Executors.defaultThreadFactory(),// 线程工厂
                handler);// 拒绝策略
    }

}
