package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.Status;
import com.atguigu.mqtt.domain.sxtlist;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SxtMapper {
    @Update("UPDATE sxt_status SET status= #{status}, time= #{time} WHERE room = #{id}")
    int updateSxtStatus(@Param("id") Integer id,@Param("status") Integer status, @Param("time") Date time);
    @Insert("insert into sxt_status(room,time,status) values(#{id},#{time},#{status})")
    void insertStatus(Status status);
    @Select("select room as id, image as address, time, person from image WHERE room = #{id} ORDER BY time DESC")
    List<sxtlist> getlist(String startTime, String endTime, Integer id);
}
