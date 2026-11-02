package com.atguigu.mqtt.controller;
import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.domain.TbLamp;
import com.atguigu.mqtt.service.KgChnageService;
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
@RequestMapping("/kg")
public class KgController {
    @Autowired
    private KgChnageService kgChnageService;
    @Autowired
    private MqttMessageSender mqttMessageSender;

    @PostMapping("/change")
    public void changeKgStatus(@RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        Integer id = request.get("id");
        if (id == null){
            throw new IllegalArgumentException("设备id不能为空");
        }

        TbLamp tbLamp = new TbLamp();
        tbLamp.setDeviceid("kg");
        tbLamp.setStatus(status);
        tbLamp.setCreateTime(new Date());
        tbLamp.setUpdateTime(new Date());
        kgChnageService.addKgStatus(tbLamp);
        Map<String , Object> map = new HashMap<>() ;
        map.put("name" , "kg") ;
        map.put("status" , status) ;
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("a/status" , json);
        mqttMessageSender.sendMsg("ID"+id+"IN" , json);
    }
}