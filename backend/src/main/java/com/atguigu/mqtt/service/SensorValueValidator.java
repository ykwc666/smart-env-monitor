package com.atguigu.mqtt.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 传感器异常值过滤：超范围数据不推送、不落库，并统计拒绝次数。
 */
@Component
public class SensorValueValidator {

    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong accepted = new AtomicLong();
    private final AtomicLong wsPushed = new AtomicLong();

    public void markWsPushed() {
        wsPushed.incrementAndGet();
    }

    public boolean isValidTemp(double v) {
        return inRange(v, -40, 85);
    }

    public boolean isValidHumi(double v) {
        return inRange(v, 0, 100);
    }

    public boolean isValidLight(double v) {
        return inRange(v, 0, 100000);
    }

    public boolean isValidCurrent(double v) {
        return inRange(v, 0, 100);
    }

    public boolean isValidVoltage(double v) {
        return inRange(v, 0, 500);
    }

    private boolean inRange(double v, double min, double max) {
        boolean ok = !Double.isNaN(v) && !Double.isInfinite(v) && v >= min && v <= max;
        if (ok) {
            accepted.incrementAndGet();
        } else {
            rejected.incrementAndGet();
        }
        return ok;
    }

    public long getRejected() {
        return rejected.get();
    }

    public long getAccepted() {
        return accepted.get();
    }

    public long getWsPushed() {
        return wsPushed.get();
    }

    public void reset() {
        rejected.set(0);
        accepted.set(0);
        wsPushed.set(0);
    }
}
