/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.util;

import java.util.HashMap;

public interface IStatsChanged {

    void onTotalStepsChanged(int totalSteps);

    void onMaxStepsChanged(long day, int maxSteps);

    void onAverageStepsChanged(int averageSteps);

    void onLast7DaysStats(HashMap<Long, Integer> last7DaysStats);
}
