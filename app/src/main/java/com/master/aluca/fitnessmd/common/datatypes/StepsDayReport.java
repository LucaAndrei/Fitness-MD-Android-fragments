package com.master.aluca.fitnessmd.common.datatypes;

/**
 * Created by aluca on 11/16/16.
 */
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
