package com.atguigu.mqtt.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
public interface dengMapper {
    @Insert("insert into deng(room,time,status,name) values(#{id},#{date},#{status},#{led})")
    void insertDeng(Integer id, Integer status, String led, Date date);
    @Update("update deng set status=#{status},time=#{date},name=#{led} where room=#{id} and name=#{led}")
    int updateDeng(Integer id, Integer status, String led, Date date);
}
