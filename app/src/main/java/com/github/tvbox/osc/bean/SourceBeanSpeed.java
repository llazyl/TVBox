package com.github.tvbox.osc.bean;

public class SourceBeanSpeed implements Comparable {
    private SourceBean bean;
    private int speed = 0;

    public SourceBeanSpeed(SourceBean bean) {
        this.bean = bean;
    }

    public void setBean(SourceBean bean) {
        this.bean = bean;
    }

    public SourceBean getBean() {
        return bean;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    @Override
    public int compareTo(Object o) {
        return this.speed - ((SourceBeanSpeed) o).speed;
    }
}
