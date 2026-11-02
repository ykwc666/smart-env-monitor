package com.atguigu.mqtt.service;

import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.wsd;

public interface WsdService {
    void SetWsdStatus(Integer id, Integer status);

    PageResult getWsdList(Integer id, Integer page, Integer pageSize);

    PageResult getWsd(Integer id, double minTemp, double maxTemp, double minhumi, double maxhumi, String startTime, String endTime, int page, int pageSize);

}
