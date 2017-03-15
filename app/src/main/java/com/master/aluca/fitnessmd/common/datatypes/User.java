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

public class User {
    private String email;
    private String password;
    private String name;
    private String gender;
    private String savedDeviceName;
    private String savedDeviceAddress;
    private String profilePictureURI;
    private float weight;
    private float weightGoal;
    private int height;
    private int yearOfBirth;
    private int alwaysEnableBT;
    private int hasProfilePicture;
    private int registrationComplete;
    private int isOnline;

    public User(String email, String password, String name, String gender, String savedDeviceName,
                String savedDeviceAddress, String profilePictureURI, float weight, float weightGoal,
                int height, int yearOfBirth, int alwaysEnableBT, int hasProfilePicture,
                int registrationComplete, int isOnline) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.savedDeviceName = savedDeviceName;
        this.savedDeviceAddress = savedDeviceAddress;
        this.profilePictureURI = profilePictureURI;
        this.weight = weight;
        this.weightGoal = weightGoal;
        this.height = height;
        this.yearOfBirth = yearOfBirth;
        this.alwaysEnableBT = alwaysEnableBT;
        this.hasProfilePicture = hasProfilePicture;
        this.registrationComplete = registrationComplete;
        this.isOnline = isOnline;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getHasProfilePicture() {
        return hasProfilePicture;
    }

    public void setHasProfilePicture(int hasProfilePicture) {
        this.hasProfilePicture = hasProfilePicture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSavedDeviceName() {
        return savedDeviceName;
    }

    public void setSavedDeviceName(String savedDeviceName) {
        this.savedDeviceName = savedDeviceName;
    }

    public String getSavedDeviceAddress() {
        return savedDeviceAddress;
    }

    public void setSavedDeviceAddress(String savedDeviceAddress) {
        this.savedDeviceAddress = savedDeviceAddress;
    }

    public String getProfilePictureURI() {
        return profilePictureURI;
    }

    public void setProfilePictureURI(String profilePictureURI) {
        this.profilePictureURI = profilePictureURI;
    }

    public float getWeightGoal() {
        return weightGoal;
    }

    public void setWeightGoal(float weightGoal) {
        this.weightGoal = weightGoal;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public int getAlwaysEnableBT() {
        return alwaysEnableBT;
    }

    public void setAlwaysEnableBT(int alwaysEnableBT) {
        this.alwaysEnableBT = alwaysEnableBT;
    }


    public int getRegistrationComplete() {
        return registrationComplete;
    }

    public void setRegistrationComplete(int registrationComplete) {
        this.registrationComplete = registrationComplete;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

}
