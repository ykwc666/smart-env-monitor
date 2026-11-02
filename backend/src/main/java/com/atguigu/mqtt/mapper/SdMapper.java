package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.Sd;
import com.atguigu.mqtt.domain.Status;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SdMapper {
    @Insert("insert into sd_status(room,time,status) values(#{id},#{time},#{status})")
    void insert(Status status);
    @Update("UPDATE sd_status SET status=#{status}, time=#{time} WHERE room=#{id}")
    int updateLatestStatus(@Param("id") Integer id,@Param("status") Integer status, @Param("time") Date time);
    @Insert("insert into sd(room,time,sd) values(#{id},#{time},#{number})")
    void insertSd(Figure figure);
    @Select("SELECT room as id, time, sd as number FROM sd WHERE room = #{id}")
    List<Figure> getSdList(Integer id);

    List<Figure> getSd(Integer id, double min, double max, String startTime, String endTime);
}
