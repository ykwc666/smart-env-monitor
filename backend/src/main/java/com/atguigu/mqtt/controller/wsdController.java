package com.atguigu.mqtt.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.Result;
import com.atguigu.mqtt.domain.wsd;
import com.atguigu.mqtt.domain.wsdDTO;
import com.atguigu.mqtt.mapper.WsdMapper;
import com.atguigu.mqtt.service.MqttMessageSender;
import com.atguigu.mqtt.service.WsdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wsd")
public class wsdController {
    @Autowired
    private WsdService wsdService;
    @Autowired
    private WsdMapper wsdMapper;
    @Autowired
    private MqttMessageSender mqttMessageSender;

    @PostMapping("/change")
    public void changeWsdStatus(@RequestBody Map<String, Integer> request){
        Integer status = request.get("status");
        Integer id = request.get("id");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("name", "wsd");
        String json = JSON.toJSONString(map);
        mqttMessageSender.sendMsg("ID"+ id+"IN", json);
        wsdService.SetWsdStatus(id,status);
    }
    @PostMapping("/list")
    public Result<PageResult> getWsdList(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        Integer page = (Integer) request.get("page");
        Integer pageSize = (Integer) request.get("pageSize");
        PageResult pageResult = wsdService.getWsdList(id, page, pageSize);
        return Result.success(pageResult);
    }
    @PostMapping("/get")
    public Result<PageResult> getWsd(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        Object minTempObj = request.get("minTemp");
        double minTemp = 0.0;
        if (minTempObj instanceof Number) {
            minTemp = ((Number) minTempObj).doubleValue();
        } else if (minTempObj instanceof String) {
            minTemp = Double.parseDouble((String) minTempObj);
        }
        Object maxhumiObj = request.get("maxHumi");
        double maxhumi = 0.0;
        if (maxhumiObj instanceof Number) {
            maxhumi = ((Number) maxhumiObj).doubleValue();
        } else if (maxhumiObj instanceof String) {
            maxhumi = Double.parseDouble((String) maxhumiObj);
        }
        Object minhumiObj = request.get("minHumi");
        double minhumi = 0.0;
        if (minhumiObj instanceof Number) {
            minhumi = ((Number) minhumiObj).doubleValue();
        } else if (minTempObj instanceof String) {
            minhumi = Double.parseDouble((String) minhumiObj);
        }
        Object maxTempObj = request.get("maxTemp");
        double maxTemp = 0.0;
        if (maxTempObj instanceof Number) {
            maxTemp = ((Number) maxTempObj).doubleValue();
        } else if (maxTempObj instanceof String) {
            maxTemp = Double.parseDouble((String) maxTempObj);
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
        System.out.println(minTemp);
        System.out.println(maxTemp);
        System.out.println(minhumi);
        System.out.println(maxhumi);
        PageResult pageResult =wsdService.getWsd(id, minTemp, maxTemp, minhumi, maxhumi, startTime, endTime, page, pageSize);
        return Result.success(pageResult);
    }

    @PostMapping("/add")
    public Result<String> addWsd(@RequestBody wsd wsd) {
        wsdMapper.addWsd(wsd);
        return Result.success("添加成功");
    }
    @PostMapping("/byid")
    public Result<wsd> getWsdById(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        System.out.println(id);
        wsd wsdList = wsdMapper.selectWsd(id);
        return Result.success(wsdList);
    }
    @PostMapping("/update")
    public Result<String> updateWsd(@RequestBody wsdDTO wsd) {
        wsdMapper.updateWd(wsd);
        return Result.success("更新成功");
    }
    @PostMapping("/delete")
    public Result<String> deleteWsd(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        wsdMapper.deleteWd(id);
        return Result.success("删除成功");
    }

}
