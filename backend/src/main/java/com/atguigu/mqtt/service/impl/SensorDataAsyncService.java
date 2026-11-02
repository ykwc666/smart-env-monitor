package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.wsd;
import com.atguigu.mqtt.mapper.GGMapper;
import com.atguigu.mqtt.mapper.WsdMapper;
import com.atguigu.mqtt.service.ElectricityService;
import com.atguigu.mqtt.service.wdChnageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

// SensorDataAsyncService.java
@Service
@Slf4j
public class SensorDataAsyncService {

    @Autowired
    private WsdMapper wsdMapper;

    @Autowired
    private GGMapper ggMapper;

    @Autowired
    private ElectricityService electricityService;

    @Autowired
    private wdChnageService wdChangeService;

    /**
     * 异步保存 kg/wd 单路温度（先推 WS，再落库，不阻塞入站线程）
     */
    @Async("sensorTaskExecutor")
    public void saveWdAsync(int id, double temperature) {
        try {
            Figure figure = new Figure();
            figure.setId(id);
            figure.setTime(new Date());
            figure.setNumber(temperature);
            wdChangeService.insertWd(figure);
            log.debug("temperature saved async: id={}, wd={}", id, temperature);
        } catch (Exception e) {
            log.error("async save temperature failed: id={}, wd={}", id, temperature, e);
        }
    }

    /**
     * 异步保存温湿度数据
     */
    @Async("sensorTaskExecutor")
    public void saveWsdAsync(int id, double wd, double sd) {
        try {
            wsd wsd = new wsd();
            wsd.setId(id);
            wsd.setTime(new Date());
            wsd.setTemp(wd);
            wsd.setHumi(sd);
            wsdMapper.insertWd(wsd);
            log.debug("wsd saved async: id={}, temp={}, humi={}", id, wd, sd);
        } catch (Exception e) {
            log.error("async save wsd failed: id={}, wd={}, sd={}", id, wd, sd, e);
        }
    }

    /**
     * 异步保存光感数据
     */
    @Async("sensorTaskExecutor")
    public void saveGgAsync(int id, double gg) {
        try {
            Figure figure = new Figure(); // 注意：这里假设 Figure 是你的实体类
            figure.setId(id);
            figure.setNumber(gg);
            figure.setTime(new Date());
            ggMapper.InsertGg(figure);
            log.debug("✅ 光感数据已异步保存: id={}, gg={}", id, gg);
        } catch (Exception e) {
            log.error("❌ 异步保存光感数据失败: id={}, gg={}", id, gg, e);
        }
    }

    /**
     * 异步保存电流电压数据
     */
    @Async("sensorTaskExecutor")
    public void saveElectricityAsync(int id, double dl, double dy) {
        try {
            electricityService.AddElectricity(id, dl, dy);
            log.debug("✅ 电感数据已异步保存: id={}, current={}, voltage={}", id, dl, dy);
        } catch (Exception e) {
            log.error("❌ 异步保存电感数据失败: id={}, dl={}, dy={}", id, dl, dy, e);
        }
    }
}
