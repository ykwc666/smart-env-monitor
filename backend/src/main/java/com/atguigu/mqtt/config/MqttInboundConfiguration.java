package com.atguigu.mqtt.config;

import com.atguigu.mqtt.domain.MqttConfigurationProperties;
import com.atguigu.mqtt.handler.ReceiverMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.concurrent.Executor;

@Configuration
public class MqttInboundConfiguration {

    @Autowired
    private MqttConfigurationProperties mqttConfigurationProperties ;

    @Autowired
    private MqttPahoClientFactory mqttPahoClientFactory ;

    @Autowired
    private ReceiverMessageHandler receiverMessageHandler ;

    @Autowired
    @Qualifier("mqttInboundExecutor")
    private Executor mqttInboundExecutor;

    // 入站走线程池，避免在 MQTT 回调线程里做推送/解析时互相阻塞
    @Bean
    public MessageChannel messageInboundChannel() {
        return new ExecutorChannel(mqttInboundExecutor);
    }

    // 配置入站适配器，作用：设置订阅主题，以及指定消息的相关属性
    @Bean
    public MessageProducer messageProducer() {
        // public MqttPahoMessageDrivenChannelAdapter(String url, String clientId, MqttPahoClientFactory clientFactory,
        //			String... topic)
        MqttPahoMessageDrivenChannelAdapter mqttPahoMessageDrivenChannelAdapter = new MqttPahoMessageDrivenChannelAdapter(
                mqttConfigurationProperties.getUrl() ,
                mqttConfigurationProperties.getSubClientId() ,
                mqttPahoClientFactory ,
                mqttConfigurationProperties.getSubTopic().split(",")
        ) ;

        mqttPahoMessageDrivenChannelAdapter.setQos(1);
        mqttPahoMessageDrivenChannelAdapter.setConverter(new DefaultPahoMessageConverter());
        mqttPahoMessageDrivenChannelAdapter.setOutputChannel(messageInboundChannel());
        return mqttPahoMessageDrivenChannelAdapter ;
    }

    @Bean
    @ServiceActivator(inputChannel = "messageInboundChannel")
    public MessageHandler messageHandler() {
        return receiverMessageHandler ;
    }

}
