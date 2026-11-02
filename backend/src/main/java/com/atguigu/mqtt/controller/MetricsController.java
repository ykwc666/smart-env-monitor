package com.atguigu.mqtt.controller;

import com.atguigu.mqtt.service.SensorValueValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    @Autowired
    private SensorValueValidator validator;

    @GetMapping("/sensor")
    public Map<String, Object> sensorMetrics() {
        Map<String, Object> m = new LinkedHashMap<>();
        long accepted = validator.getAccepted();
        long rejected = validator.getRejected();
        long total = accepted + rejected;
        m.put("accepted", accepted);
        m.put("rejected", rejected);
        m.put("wsPushed", validator.getWsPushed());
        m.put("total", total);
        m.put("rejectRate", total == 0 ? 0.0 : Math.round(rejected * 10000.0 / total) / 100.0);
        return m;
    }

    @PostMapping("/sensor/reset")
    public Map<String, Object> reset() {
        validator.reset();
        return sensorMetrics();
    }
}
