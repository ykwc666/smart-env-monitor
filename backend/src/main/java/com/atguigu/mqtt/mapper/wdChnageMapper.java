package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.Status;
import com.atguigu.mqtt.domain.Wd;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface wdChnageMapper {
    @Insert("insert into wd(room,wd,time) values(#{id},#{number},#{time})")
    void insert(Figure figure);
    @Delete("delete from wd")
    void deleteWd();
    @Update("UPDATE wd_status SET status= #{status}, time= #{time} WHERE room = #{id}")
    int updateWdStatus(Status status);
    @Insert("insert into wd_status(room,time,status) values(#{id},#{time},#{status})")
    void insertWdStatus(Status status);
    @Select("select room as id,time, wd as number   from wd where room= #{id}")
    List<Figure> getWdList(Integer id);

    List<Figure> getWd(Integer id,double minTemp, double maxTemp, String startTime, String endTime);
}
