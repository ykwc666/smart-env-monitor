package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.Figure;
import com.atguigu.mqtt.domain.PageResult;
import com.atguigu.mqtt.domain.Status;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface GGMapper {
    @Update("UPDATE gg_status SET status=#{status}, time=#{time} WHERE room = #{id}")
    int update(@Param("id") Integer id,@Param("status") Integer status, @Param("time") Date time);
    @Insert("insert into gg_status(room,time,status) values(#{id},#{time},#{status})")
    void insert(Status status);
    @Insert("insert into gg(room,time,gg) values(#{id},#{time},#{number})")
    void InsertGg(Figure  figure);
    @Select("select  room as id, time, gg as number FROM  gg where room= #{id}  ORDER BY time DESC")
    Page<Figure> getGgList(Integer id);

    Page<Figure> getGg(Integer id, double min, double max, String startTime, String endTime);
}
