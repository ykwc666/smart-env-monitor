package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.Electricity;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface ElectricityMapper {
    @Update("UPDATE electricity_status SET status= #{status}, time= #{time} WHERE room = #{id}")
    int updateElectricityStatus(@Param("id") Integer id,@Param("status") Integer status, @Param("time") Date time);
    @Insert("insert into electricity_status(room,time,status) values(#{id},#{time},#{status})")
    void insertElectricityStatus(@Param("id") Integer id,@Param("status") Integer status, @Param("time") Date time);
    @Insert("insert into electricity(room,time,current,voltage) values(#{id},#{time},#{current},#{voltage})")
    void insertElectricity(Electricity electricity);
    @Select("select room as id, time,current,voltage from electricity where room=#{id}  ORDER BY time DESC")
    Page<Electricity> getElectricityList(Integer id);

    Page<Electricity> getElectricity(Integer id, double mindl, double maxdl, double mindy, double maxdy, String startTime, String endTime);
}
