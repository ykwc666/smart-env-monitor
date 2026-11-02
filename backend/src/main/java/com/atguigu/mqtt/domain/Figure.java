package com.atguigu.mqtt.domain;

import lombok.Data;

import java.util.Date;
@Data
public class Figure {
    private Integer id;
    private Date time;
    private Double number;
}
