package com.atguigu.mqtt.test;

import com.atguigu.mqtt.MqttClientApplication;
import com.atguigu.mqtt.service.MqttMessageSender;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.Reader;

@SpringBootTest(classes = MqttClientApplication.class)
public class MqttMessageSenderTest {

    @Autowired
    private MqttMessageSender mqttMessageSender ;

    @Test
    public void sendToMsg() {
        mqttMessageSender.sendMsg("java/c" , "hello mqtt spring boot...");
    }
    @Test
    public void sendToMsg2() {
        String reasources = "mybatis-config.xml";
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(reasources);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        String sql = "select * from user where id = ?";
        Object object = sqlSession.selectOne(sql, 1);
        System.out.println(object);
    }

}
