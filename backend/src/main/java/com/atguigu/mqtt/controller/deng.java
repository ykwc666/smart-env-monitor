package com.atguigu.mqtt.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.mapper.dengMapper;
import com.atguigu.mqtt.service.MqttMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/deng")
public class deng {
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @Autowired
    private dengMapper dengMapper;
    @PostMapping("/LED/change")
    public void changeDengStatus(@RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null || id == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", "led");
        map.put("status", status);
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+ id+"IN", json);
        //数据库添加
        if(dengMapper.updateDeng(id,status,"LED",new Date())==0){
           dengMapper.insertDeng(id,status,"LED",new Date());
        }
    }
    @PostMapping("/dp/change")
    public void changeDpStatus(@RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null || id == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", "dp");
        map.put("status", status);
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+ id+"IN", json);
        //数据库添加
        if (dengMapper.updateDeng(id,status,"DP",new Date())==0){
            dengMapper.insertDeng(id,status,"DP",new Date());
        }
    }
}
