package com.atguigu.mqtt.mapper;

import com.atguigu.mqtt.domain.Status;
import com.atguigu.mqtt.domain.wsd;
import com.atguigu.mqtt.domain.wsdDTO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface WsdMapper {
    @Update("UPDATE wsd_status SET status= #{status}, time= #{time} WHERE room = #{id}")
    int updateWdStatus(Status status1);
    @Insert("insert into wsd_status(room,time,status) values(#{id},#{time},#{status})")
    void insertWdStatus(Status status1);
    @Select("select id as byid,room as id,time, wd as temp, sd as humi   from wsd where room= #{id}  ORDER BY time DESC")
    Page<wsdDTO> getWsdList(Integer id);

    Page<wsdDTO> getWsd(Integer id, double minTemp, double maxTemp, double minhumi, double maxhumi ,String startTime, String endTime);
    @Insert("insert into wsd(room,time,wd,sd) values(#{id},#{time},#{temp},#{humi})  ")
    void insertWd(wsd wsd);
    @Insert("insert into wsd(room,time,wd,sd) values(#{id},#{time},#{temp},#{humi}) ")
    void addWsd(wsd wsd);
    @Select("select room as id,time, wd as temp, sd as humi   from wsd where id= #{id}  ORDER BY time DESC")
    wsd selectWsd(Integer id);
    @Update("update wsd set room= #{id},time= #{time},wd= #{temp},sd= #{humi} where id= #{byid} ")
    void updateWd(wsdDTO wsd);
    @Delete("delete from wsd where id= #{id}")
    void deleteWd(Integer id);
}
