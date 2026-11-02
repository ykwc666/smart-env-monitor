package com.atguigu.mqtt.service;

import com.atguigu.mqtt.domain.Electricity;
import com.atguigu.mqtt.domain.PageResult;

import java.util.List;

public interface ElectricityService {
    void SetElectricityStatus(Integer id,Integer status);
    void AddElectricity(int id,double dl ,double dy);

    PageResult getElectricityList(Integer id, Integer page, Integer pageSize);

    PageResult getElectricity(Integer id, double mindl, double maxdl, double mindy, double maxdy, String startTime, String endTime, int page, int pageSize);
}
