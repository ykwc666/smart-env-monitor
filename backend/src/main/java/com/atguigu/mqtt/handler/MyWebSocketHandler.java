package com.atguigu.mqtt.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Random;

public class MyWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket 连接已建立: " + session.getId());

        // 发送欢迎消息
        session.sendMessage(new TextMessage("{\"status\": \"connected\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);

        // 模拟湿度数据发送
        if ("requestData".equals(payload)) {
            Random random = new Random();
            int humidity = random.nextInt(100);
            String response = "{\"sd\": " + humidity + "}";
            session.sendMessage(new TextMessage(response));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("WebSocket 连接关闭: " + session.getId());
    }
}
