package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.Electricity;
import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.wsd;
import com.atguigu.mqtt.mapper.TableMapper;
import com.atguigu.mqtt.mapper.WsdMapper;
import com.atguigu.mqtt.service.tableService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class tableServiceImpl implements tableService {
    @Autowired
    private TableMapper tableMapper;
    public PageResult getWsd(List<Integer> id, double maxTemp, double maxHumi, String startTime, String endTime, int page, int pageSize){
        PageHelper.startPage(page, pageSize);
        Page<wsd> wsdList = tableMapper.getWsd(id,maxTemp,maxHumi,startTime,endTime);
        return new PageResult(wsdList.getTotal(), wsdList.getResult());
    }
    public PageResult getGg(List<Integer> id, double maxLight, String startTime, String endTime, int page, int pageSize){
        PageHelper.startPage(page, pageSize);
        Page<Figure> ggList = tableMapper.getGg(id,maxLight,startTime,endTime);
        return new PageResult(ggList.getTotal(), ggList.getResult());
    }
    public PageResult getElectricity(List<Integer> id, double maxElec, double maxVoltage, String startTime, String endTime, int page, int pageSize){
        PageHelper.startPage(page, pageSize);
        Page<Electricity> electricityList = tableMapper.getElectricity(id,maxElec,maxVoltage,startTime,endTime);
        return new PageResult(electricityList.getTotal(), electricityList.getResult());
    }
}
