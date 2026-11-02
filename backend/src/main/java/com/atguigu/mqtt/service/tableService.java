package com.atguigu.mqtt.service;

import com.atguigu.mqtt.domain.PageResult;

import java.util.List;

public interface tableService {
    PageResult getWsd(List<Integer> id, double maxTemp, double maxHumi, String startTime, String endTime, int page, int pageSize);

    PageResult getGg(List<Integer> id, double maxLight, String startTime, String endTime, int page, int pageSize);

    PageResult getElectricity(List<Integer> id, double maxElec, double maxVoltage, String startTime, String endTime, int page, int pageSize);
}
