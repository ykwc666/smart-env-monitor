package com.atguigu.mqtt.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.Result;
import com.atguigu.mqtt.mapper.GGMapper;
import com.atguigu.mqtt.service.GGService;
import com.atguigu.mqtt.service.MqttMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/gg")
public class ggController {
    @Autowired
    private GGService ggService;
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @Autowired
    private GGMapper ggMapper;
    @RequestMapping("/change")
    public void changeGgStatus(@RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        ggService.SetGgStatus(id,status);
        Map<String, Object> map = new HashMap<>();
        map.put("name","gg");
        map.put("status", status);
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+ id+"IN", json);
    }
    @PostMapping("/list")
    public Result<PageResult> getGgList(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        Integer page = (Integer) request.get("page");
        Integer pageSize = (Integer) request.get("pageSize");
        PageResult pageResult = ggService.getGgList(id, page, pageSize);
        return Result.success(pageResult);
    }
    @PostMapping("/get")
    public Result<PageResult> getGg(@RequestBody Map<String, Object> request) {
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
        int page = (Integer) request.get("page");
        int pageSize = (Integer) request.get("pageSize");
        System.out.println(min);
        System.out.println(max);
        PageResult pageResult = ggService.getGg(id, min, max, startTime, endTime, page, pageSize);
        return Result.success(pageResult);
    }
}
