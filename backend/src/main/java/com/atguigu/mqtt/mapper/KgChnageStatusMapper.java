package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.TbLamp;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KgChnageStatusMapper {
    @Insert("insert into tb_lamp(deviceid,status,create_time,update_time) values(#{deviceid},#{status},#{createTime},#{updateTime})")
    void insert(TbLamp tbLamp);
}
