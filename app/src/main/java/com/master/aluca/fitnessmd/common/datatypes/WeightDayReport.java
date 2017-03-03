/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.datatypes;

import com.master.aluca.fitnessmd.common.Constants;

public class WeightDayReport {
    private float weight;
    private long day;

    public WeightDayReport(float weight, long day) {
        this.weight = weight;
        this.day = day;
    }

    public WeightDayReport() {
        // TODO - for testing purposes
        // to see what fits best in production
        this.day = System.currentTimeMillis();
        this.weight = Constants.WEIGHT_DEFAULT_VALUE;
    }

    public float getWeight() {
        return weight;
    }

    public long getDay() {
        return day;
    }

    public void setDay(long day) {
        this.day = day;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
