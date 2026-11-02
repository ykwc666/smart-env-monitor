package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.domain.*;
import com.atguigu.mqtt.domain.wsdDTO;
import com.atguigu.mqtt.mapper.WsdMapper;
import com.atguigu.mqtt.service.WsdService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class wsdServiceImpl implements WsdService {
    @Autowired
    private WsdMapper wsdMapper;
    public void SetWsdStatus(Integer id,Integer  status)
    {
        Status status1 = new Status();
        status1.setId(id);
        status1.setStatus(status);
        status1.setTime(new Date());
        int update = wsdMapper.updateWdStatus(status1);
        if(update == 0)
        {
            wsdMapper.insertWdStatus(status1);
        }
    }

    public PageResult getWsdList(Integer id,Integer page,Integer pageSize){
        PageHelper.startPage(page, pageSize);
        Page<wsdDTO> wsdList = wsdMapper.getWsdList(id);
        return new PageResult(wsdList.getTotal(), wsdList.getResult());
    }
    public PageResult getWsd(Integer id, double minTemp, double maxTemp, double minhumi, double maxhumi, String startTime, String endTime, int page, int pageSize){
        PageHelper.startPage(page, pageSize);
        System.out.println(minTemp);
        System.out.println(maxTemp);
        System.out.println(minhumi);
        System.out.println(maxhumi);
        Page<wsdDTO> wsdList = wsdMapper.getWsd(id,minTemp,maxTemp,minhumi,maxhumi,startTime,endTime);
        System.out.println(wsdList);
        return new PageResult(wsdList.getTotal(), wsdList.getResult());
    }
}
