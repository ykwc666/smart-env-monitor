package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.Electricity;
import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.wsdDTO;
import com.atguigu.mqtt.mapper.ElectricityMapper;
import com.atguigu.mqtt.service.ElectricityService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ElectricityServiceImpl implements ElectricityService {
    @Autowired
    private ElectricityMapper electricityMapper;
    @Override
    public void SetElectricityStatus(Integer id,Integer status) {
     int update = electricityMapper.updateElectricityStatus(id,status,new Date());
     if(update == 0)
     {
         electricityMapper.insertElectricityStatus(id,status,new Date());
     }
    }
    @Override
    public void AddElectricity(int id  ,double dl,double dy){
        Electricity electricity = new Electricity();
        electricity.setId(id);
        electricity.setTime(new Date());
        electricity.setCurrent(dl);
        electricity.setVoltage(dy);
        electricityMapper.insertElectricity(electricity);
    }
    @Override
    public PageResult getElectricityList(Integer id,Integer page,Integer pageSize){
        PageHelper.startPage(page, pageSize);
        Page<Electricity> eleList = electricityMapper.getElectricityList(id);
        return new PageResult(eleList.getTotal(), eleList.getResult());
    }
    @Override
    public PageResult getElectricity(Integer id, double mindl, double maxdl, double mindy, double maxdy, String startTime, String endTime, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        Page<Electricity> electricityList = electricityMapper.getElectricity(id,mindl, maxdl, mindy, maxdy, startTime, endTime);
        return new PageResult(electricityList.getTotal(), electricityList.getResult());
    }
}
