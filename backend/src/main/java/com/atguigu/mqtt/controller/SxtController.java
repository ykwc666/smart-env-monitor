package com.atguigu.mqtt.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.domain.Result;
import com.atguigu.mqtt.domain.sxtlist;
import com.atguigu.mqtt.mapper.SxtMapper;
import com.atguigu.mqtt.service.MqttMessageSender;
import com.atguigu.mqtt.service.SxtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sxt")
public class SxtController {

    @Autowired
    private SxtService sxtService;
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @Autowired
    private SxtMapper sxtMapper;

    @PostMapping("/change")
    public void changeSxtStatus(@RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        sxtService.changeSxtStatus(id,status);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "sxt");
        map.put("status", status);
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+id+"IN", json);
    }
    @PostMapping("/list")
    public Result<List<sxtlist>> getsxtlist(@RequestBody  Map<String, Object> request){
        Integer id = (Integer) request.get("id");
        List<String> timeRange = (List<String>) request.get("timeRange");
        String startTime;
        String endTime;
        if(timeRange == null || timeRange.isEmpty() || timeRange.size() < 2){
            startTime="2000-08-01 00:00:00";
            endTime="2099-09-05 00:00:00";
        } else {
            startTime = timeRange.get(0);
            endTime = timeRange.get(1);
        }
        return Result.success(sxtMapper.getlist(startTime,endTime,id));
    }
}
