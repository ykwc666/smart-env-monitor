package com.atguigu.mqtt.domain;
import lombok.Data;
import java.util.Date;

@Data
public class Status {
    private Integer id;
    private Date time;
    private Integer status;
}
