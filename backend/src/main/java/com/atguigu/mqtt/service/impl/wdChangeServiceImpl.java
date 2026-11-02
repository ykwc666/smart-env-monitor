package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.Status;
import com.atguigu.mqtt.domain.TbLampStatus;
import com.atguigu.mqtt.domain.Wd;
import com.atguigu.mqtt.mapper.TbLampStatusMapper;
import com.atguigu.mqtt.mapper.wdChnageMapper;
import com.atguigu.mqtt.service.wdChnageService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class wdChangeServiceImpl implements wdChnageService {
    @Autowired
    private TbLampStatusMapper tbLampStatusMapper;
    @Autowired
    private wdChnageMapper wdChangeMapper;
    @Override
    public void addWdStatus(Integer id,Integer  status) {
        Status status1 = new Status();
        status1.setId(id);
        status1.setStatus(status);
        status1.setTime(new Date());
        int update = wdChangeMapper.updateWdStatus(status1);
        if(update == 0)
        {

            wdChangeMapper.insertWdStatus(status1);
        }
    }
    @Override
    public void insertWd(Figure  figure) {
        wdChangeMapper.insert(figure);
    }
    @Override
    public void deleteWd() {
        wdChangeMapper.deleteWd();
    }



}
