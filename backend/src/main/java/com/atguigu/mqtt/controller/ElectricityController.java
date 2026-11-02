package com.atguigu.mqtt.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.domain.Electricity;
import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.Result;
import com.atguigu.mqtt.mapper.ElectricityMapper;
import com.atguigu.mqtt.service.ElectricityService;
import com.atguigu.mqtt.service.MqttMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/electricity")
public class ElectricityController {
    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private ElectricityMapper electricityMapper;
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @PostMapping("/change")
    public void changeElectricityStatus(@RequestBody Map<String, Integer> request){
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        electricityService.SetElectricityStatus(id,status);
        Map<String, Object> map = new HashMap<>();
        map.put("name","dg");
        map.put("status", status);
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+ id+"IN", json);
    }
    @PostMapping("/list")
    public Result<PageResult> getElectricityList(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        Integer page = (Integer) request.get("page");
        Integer pageSize = (Integer) request.get("pageSize");
        PageResult pageResult = electricityService.getElectricityList(id, page, pageSize);
        return Result.success(pageResult);
    }
    @PostMapping("/get")
    public Result<PageResult> getElectricity(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        Object mincurrentObj = request.get("mincurrent");
        double mindl = 0.0;
        if (mincurrentObj instanceof Number) {
            mindl = ((Number) mincurrentObj).doubleValue();
        } else if (mincurrentObj instanceof String) {
            mindl = Double.parseDouble((String) mincurrentObj);
        }
        Object maxcurrentObj = request.get("maxcurrent");
        double maxdl = 0.0;
        if (maxcurrentObj instanceof Number) {
            maxdl = ((Number) maxcurrentObj).doubleValue();
        } else if (maxcurrentObj instanceof String) {
            maxdl = Double.parseDouble((String) maxcurrentObj);
        }
        Object minvoltageObj = request.get("minvoltage");
        double mindy = 0.0;
        if (minvoltageObj instanceof Number) {
            mindy = ((Number) minvoltageObj).doubleValue();
        } else if (minvoltageObj instanceof String) {
            mindy = Double.parseDouble((String) minvoltageObj);
        }
        Object maxvoltageObj = request.get("maxvoltage");
        double maxdy = 0.0;
        if (maxvoltageObj instanceof Number) {
            maxdy = ((Number) maxvoltageObj).doubleValue();
        } else if (maxvoltageObj instanceof String) {
            maxdy = Double.parseDouble((String) maxvoltageObj);
        }
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
        System.out.println(mindl);
        System.out.println(maxdl);
        System.out.println(mindy);
        System.out.println(maxdy);
        PageResult pageResult = electricityService.getElectricity(id, mindl, maxdl, mindy, maxdy, startTime, endTime, page, pageSize);
        return Result.success(pageResult);
    }
}
