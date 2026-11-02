package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.Status;
import com.atguigu.mqtt.mapper.GGMapper;
import com.atguigu.mqtt.service.GGService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class GGServiceImpl implements GGService {
    @Autowired
    private GGMapper ggMapper;
    public void SetGgStatus(Integer id,Integer payload)
    {
       int update = ggMapper.update(id,payload, new Date());
       if(update == 0)
       {
           Status status = new Status();
           status.setId(id);
           status.setStatus(payload);
           status.setTime(new Date());
           ggMapper.insert(status);
       }
    }
    public PageResult getGgList(Integer id,Integer page,Integer pageSize)
    {
        PageHelper.startPage(page, pageSize);
        Page<Figure> ggList = ggMapper.getGgList(id);
        PageResult pageResult = new PageResult(ggList.getTotal(), ggList.getResult());
        return pageResult;
    }
    public PageResult getGg(Integer id, double min, double max, String startTime, String endTime, int page, int pageSize)
    {
        PageHelper.startPage(page, pageSize);
        Page<Figure> ggList = ggMapper.getGg(id,min,max,startTime,endTime);
        PageResult pageResult = new PageResult(ggList.getTotal(), ggList.getResult());
        return pageResult;
    }
}
