package com.atguigu.mqtt.controller;

import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.Result;
import com.atguigu.mqtt.service.tableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/table")
public class tableController {
    @Autowired
    private tableService tableService;
    @PostMapping("/search")
    public Result<PageResult> searchTable(@RequestBody Map<String, Object> request){
        Integer originalId = (Integer) request.get("id");
        List<Integer> targetIds = new ArrayList<>();
        // 处理id=0的情况
        if (originalId != null && originalId == 0) {
            targetIds.add(1);
            targetIds.add(2);
            targetIds.add(3);
        } else {
            targetIds.add(originalId); // 正常情况保持原id
        }
        System.out.println(targetIds);
        String name = (String) request.get("name");
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
        if(name.equals("温湿度")){
            System.out.println(name);
            Object maxTempObj = request.get("maxTemp");
            double maxTemp = 0.0;
            if (maxTempObj instanceof Number) {
                maxTemp = ((Number) maxTempObj).doubleValue();
            } else if (maxTempObj instanceof String) {
                maxTemp = Double.parseDouble((String) maxTempObj);
            }
            Object maxHumiObj = request.get("maxHumi");
            double maxHumi = 0.0;
            if (maxHumiObj instanceof Number) {
                maxHumi = ((Number) maxHumiObj).doubleValue();
            } else if (maxHumiObj instanceof String) {
                maxHumi = Double.parseDouble((String) maxHumiObj);
            }
            PageResult pageResult = tableService.getWsd(targetIds, maxTemp, maxHumi, startTime, endTime, page, pageSize);
            return Result.success(pageResult);
        }else if(name.equals("光照")){
            Object maxLightObj = request.get("maxLight");
            double maxLight = 0.0;
            if (maxLightObj instanceof Number) {
                maxLight = ((Number) maxLightObj).doubleValue();
            } else if (maxLightObj instanceof String) {
                maxLight = Double.parseDouble((String) maxLightObj);
            }
            PageResult pageResult = tableService.getGg(targetIds, maxLight, startTime, endTime, page, pageSize);
            return Result.success(pageResult);
        }else if(name.equals("电流电压")){
            Object maxElecObj = request.get("maxcurrent");
            double maxElec = 0.0;
            if (maxElecObj instanceof Number) {
                maxElec = ((Number) maxElecObj).doubleValue();
            } else if (maxElecObj instanceof String) {
                maxElec = Double.parseDouble((String) maxElecObj);
            }
            Object maxVoltageObj = request.get("maxVoltage");
            double maxVoltage = 0.0;
            if (maxVoltageObj instanceof Number) {
                maxVoltage = ((Number) maxVoltageObj).doubleValue();
            } else if (maxVoltageObj instanceof String) {
                maxVoltage = Double.parseDouble((String) maxVoltageObj);
            }
            PageResult pageResult = tableService.getElectricity(targetIds, maxElec, maxVoltage, startTime, endTime, page, pageSize);
            return Result.success(pageResult);
        }
        return Result.error("参数错误");
    }
}
