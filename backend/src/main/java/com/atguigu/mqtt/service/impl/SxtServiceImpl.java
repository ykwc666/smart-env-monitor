package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.Status;
import com.atguigu.mqtt.mapper.SxtMapper;
import com.atguigu.mqtt.service.SxtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SxtServiceImpl implements SxtService {
    @Autowired
    private SxtMapper sxtMapper;
    public void changeSxtStatus(Integer id,Integer payload)
    {
        int update = sxtMapper.updateSxtStatus(id,payload,new Date());
        if(update == 0){
            Status status = new Status();
            status.setId(id);
            status.setStatus(payload);
            status.setTime(new Date());
            sxtMapper.insertStatus(status);
        }
    }

}
