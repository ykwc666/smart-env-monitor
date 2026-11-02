package com.atguigu.mqtt.domain;

import lombok.Data;

import java.util.Date;

@Data
public class wsd {
    private int id;
    private double temp;
    private double humi;
    private Date time;
}
