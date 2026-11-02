package com.atguigu.mqtt.service;

import com.atguigu.mqtt.domain.PageResult;

public interface GGService {
    void SetGgStatus(Integer id,Integer status);

    PageResult getGgList(Integer id, Integer page, Integer pageSize);

    PageResult getGg(Integer id, double min, double max, String startTime, String endTime, int page, int pageSize);
}
