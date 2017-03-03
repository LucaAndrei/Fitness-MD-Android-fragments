/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.library;
/*
 * Copyright (c) delight.im <info@delight.im>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.Log;

/** Provides a single access point to the `Meteor` class that can be used across `Activity` instances */
public class FitnessMDMeteor {

    private static Meteor mInstance;



    private static final String LOG_TAG = "Fitness_MDMeteor";

    public synchronized static Meteor createInstance(final Context context, final String serverUri) {
        if (mInstance != null) {
            throw new IllegalStateException("An instance has already been created");
        }
        Log.d(LOG_TAG, "createInstance : " + serverUri);
        mInstance = new Meteor(context, serverUri);

        return mInstance;
    }

    public synchronized static boolean hasInstance() {
        return mInstance != null;
    }

    public synchronized static Meteor getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Please call `createInstance(...)` first");
        }

        return mInstance;
    }

    public synchronized static void destroyInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Please call `createInstance(...)` first");
        }

        mInstance.disconnect();
        mInstance.removeCallbacks();
        mInstance = null;
    }


}
