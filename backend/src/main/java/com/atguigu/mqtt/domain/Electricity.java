package com.atguigu.mqtt.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Electricity {
    private Date time;
    private double current;
    private double voltage;
    private int id;
    private int byid;
}
