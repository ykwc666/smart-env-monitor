package com.atguigu.mqtt.service;

import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.TbLampStatus;
import com.atguigu.mqtt.domain.Wd;

import java.util.List;

public interface wdChnageService {
    void addWdStatus(Integer id,Integer  status);

    void insertWd(Figure  figure);

    void deleteWd();

}
