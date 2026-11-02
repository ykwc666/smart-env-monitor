package com.atguigu.mqtt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

// AsyncConfig.java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "sensorTaskExecutor")
    public Executor sensorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("sensor-async-");
        // 队列满时由调用线程执行，避免直接丢消息；配合入站 ExecutorChannel 尽量不堵 MQTT 回调
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /** MQTT 入站消息处理线程池：解析 / 推送与 EMQX 回调解耦 */
    @Bean(name = "mqttInboundExecutor")
    public Executor mqttInboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(4000);
        executor.setThreadNamePrefix("mqtt-inbound-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
