package com.atguigu.mqtt.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.Result;
import com.atguigu.mqtt.domain.TbLampStatus;
import com.atguigu.mqtt.domain.Wd;
import com.atguigu.mqtt.mapper.wdChnageMapper;
import com.atguigu.mqtt.service.MqttMessageSender;
import com.atguigu.mqtt.service.wdChnageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/wd")
public class wdController {
    @Autowired
    private wdChnageService wdChangeService ;
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @Autowired
    private wdChnageMapper wdChangeMapper;

    @PostMapping("/change")
    public void changeWdStatus(@RequestBody Map<String, Integer> request){
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        wdChangeService.addWdStatus(id,status) ;
        if(status == 0){
            wdChangeService.deleteWd() ;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", "wd");
        map.put("status", status);
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+ id+"IN", json);
    }
    @GetMapping("/list/{id}")
    public Result<List<Figure>> getWdList(@PathVariable Integer id) {
        return Result.success(wdChangeMapper.getWdList(id));
    }
    @PostMapping("/get")
    public Result<List<Figure>> getwd(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        // 正确获取 minTemp
        Object minTempObj = request.get("minTemp");
        double minTemp = 0.0;
        if (minTempObj instanceof Number) {
            minTemp = ((Number) minTempObj).doubleValue();
        } else if (minTempObj instanceof String) {
            minTemp = Double.parseDouble((String) minTempObj);
        }

        // 正确获取 maxTemp
        Object maxTempObj = request.get("maxTemp");
        double maxTemp = 0.0;
        if (maxTempObj instanceof Number) {
            maxTemp = ((Number) maxTempObj).doubleValue();
        } else if (maxTempObj instanceof String) {
            maxTemp = Double.parseDouble((String) maxTempObj);
        }

        // 正确获取 timeRange
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
        return Result.success(wdChangeMapper.getWd(id,minTemp, maxTemp, startTime, endTime));
    }
}
