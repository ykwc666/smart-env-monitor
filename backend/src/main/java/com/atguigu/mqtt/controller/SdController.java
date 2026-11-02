package com.atguigu.mqtt.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.Result;
import com.atguigu.mqtt.domain.Sd;
import com.atguigu.mqtt.mapper.SdMapper;
import com.atguigu.mqtt.service.MqttMessageSender;
import com.atguigu.mqtt.service.SdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sd")
public class SdController {
    @Autowired
    private SdService sdService;
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @Autowired
    private SdMapper sdMapper;
    @PostMapping("/change")
    public void changeSdStatus(@RequestBody Map<String, Integer> request)
    {
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        sdService.SetSdStatus(id,status);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "sd");
        map.put("status", status);
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+ id+"IN", json);
    }
    @GetMapping("/list/{id}")
    public Result<List<Figure>> getSdList(@PathVariable Integer id) {
       return Result.success(sdMapper.getSdList(id));
    }
    @PostMapping("/get")
    public Result<List<Figure>> getSd(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        Object minObj = request.get("min");
        double min = 0.0;
        if (minObj instanceof Number) {
            min = ((Number) minObj).doubleValue();
        } else if (minObj instanceof String) {
            min = Double.parseDouble((String) minObj);
        }

        // 正确获取 maxTemp
        Object maxObj = request.get("max");
        double max = 0.0;
        if (maxObj instanceof Number) {
            max = ((Number) maxObj).doubleValue();
        } else if (maxObj instanceof String) {
            max = Double.parseDouble((String) maxObj);
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
        System.out.println(min);
        System.out.println(max);
        System.out.println(startTime);
        System.out.println(endTime);

        return Result.success(sdMapper.getSd(id, min, max, startTime, endTime));
    }

}
