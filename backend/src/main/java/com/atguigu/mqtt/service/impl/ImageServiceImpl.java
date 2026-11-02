package com.atguigu.mqtt.service.impl;

import com.atguigu.mqtt.mapper.ImageMapper;
import com.atguigu.mqtt.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    private ImageMapper imageMapper;
    @Override
    public void addImage(int room, String string,Integer person){
        imageMapper.addImage(room,string,new Date(),person);
        System.out.println("图片保存成功！");
    }
}
