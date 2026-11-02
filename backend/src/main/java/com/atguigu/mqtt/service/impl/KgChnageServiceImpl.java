package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.TbLamp;
import com.atguigu.mqtt.mapper.KgChnageStatusMapper;
import com.atguigu.mqtt.service.KgChnageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KgChnageServiceImpl implements KgChnageService {
    @Autowired
    private KgChnageStatusMapper kgChnageStatusMapper;
    public void addKgStatus(TbLamp tbLamp) {
        System.out.println("添加KG灯的状态");
        kgChnageStatusMapper.insert(tbLamp);
    }
}
