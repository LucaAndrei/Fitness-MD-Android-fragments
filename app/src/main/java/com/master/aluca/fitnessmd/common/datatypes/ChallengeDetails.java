/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.datatypes;
public class ChallengeDetails {

    public static final String DIFFICULTY = "difficulty";
    public static final String TYPE ="type";
    public static final String TEXT ="text";
    public static final String REGISTERED_USERS ="registeredUsers";


    public String getChallengeDocID() {
        return challengeDocID;
    }

    public void setChallengeDocID(String challengeDocID) {
        this.challengeDocID = challengeDocID;
    }

    String challengeDocID;
    String difficulty;
    String type;
    String text;

    public ChallengeDetails(String difficulty, String type, String text) {
        this.difficulty = difficulty;
        this.type = type;
        this.text = text;
    }

    public String[] getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(String[] registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    String[] registeredUsers;

    public ChallengeDetails(String difficulty, String type, String text, String[] registeredUsers) {
        this.difficulty = difficulty;
        this.type = type;
        this.text = text;
        this.registeredUsers = registeredUsers;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}