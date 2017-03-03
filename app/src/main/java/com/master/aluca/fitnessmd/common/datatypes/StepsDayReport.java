/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.datatypes;

public class StepsDayReport {
    private int steps;
    private long day;
    private long timeActive;

    public StepsDayReport(int steps, long day) {
        this.steps = steps;
        this.day = day;
        this.timeActive = -1;
    }
    public StepsDayReport(int steps, long day, long timeActive) {
        this.steps = steps;
        this.day = day;
        this.timeActive = timeActive;
    }

    public StepsDayReport() {

    }

    public int getSteps() {
        return steps;
    }

    public long getDay() {
        return day;
    }

    public void setDay(long day) {
        this.day = day;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public long getTimeActive() {
        return timeActive;
    }

    public void setTimeActive(long timeActive) {
        this.timeActive = timeActive;
    }
}
