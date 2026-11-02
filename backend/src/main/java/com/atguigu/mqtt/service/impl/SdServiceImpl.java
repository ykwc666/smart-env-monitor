package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.Status;
import com.atguigu.mqtt.mapper.SdMapper;
import com.atguigu.mqtt.service.SdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SdServiceImpl implements SdService {
    @Autowired
    private SdMapper sdMapper;
    public void SetSdStatus(Integer id,Integer payload) {
        // 尝试更新最新记录（通过固定ID或特殊标识）
        int updated = sdMapper.updateLatestStatus(id,payload, new Date());

        // 如果没有更新到记录（说明表为空），则插入
        if(updated == 0) {
            Status status = new Status();
            status.setId(id);
            status.setStatus(payload);
            status.setTime(new Date());
            sdMapper.insert(status);
        }
    }
}
