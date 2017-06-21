/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.datatypes;
public class AdviceDetails {
    public static final String OWNER = "ownerName";
    public static final String TIMESTAMP ="timestamp";
    public static final String MESSAGE = "message";

    String ownerName;
    String timestamp;
    String message;

    public AdviceDetails(String ownerName, String timestamp, String message) {
        this.ownerName = ownerName;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getOwnerName() {

        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}