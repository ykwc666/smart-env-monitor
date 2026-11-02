package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.Electricity;
import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.wsd;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TableMapper {
    Page<wsd> getWsd(List<Integer> id, double maxTemp, double maxHumi, String startTime, String endTime);

    Page<Figure> getGg(List<Integer> id, double maxLight, String startTime, String endTime);

    Page<Electricity> getElectricity(List<Integer> id, double maxElec, double maxVoltage, String startTime, String endTime);
}
