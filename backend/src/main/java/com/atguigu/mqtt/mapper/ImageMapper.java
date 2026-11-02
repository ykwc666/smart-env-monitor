package com.atguigu.mqtt.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface ImageMapper {
    @Insert("insert into image(room,image,time,person) values(#{room},#{string},#{time},#{person})")
    void addImage(@Param("room") int room,@Param("string") String string, Date  time,Integer person);
}
