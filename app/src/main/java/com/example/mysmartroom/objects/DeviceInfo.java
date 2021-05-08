package com.example.mysmartroom.objects;

import androidx.annotation.NonNull;

public class DeviceInfo {
    private String type;
    private String name;
    private int lightValue;
    private int servoLeft;
    private int servoRight;
    private boolean autoTurn;
    private int closeValue;
    private int openValue;
    private int brightValue;
    private String networkName;
    private String networkPassword;

    public int getBrightValue() {
        return brightValue;
    }

    public void setBrightValue(int brightValue) {
        this.brightValue = brightValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoTurn() {
        return autoTurn;
    }

    public int getCloseValue() {
        return closeValue;
    }

    public int getLightValue() {
        return lightValue;
    }

    public int getOpenValue() {
        return openValue;
    }

    public int getLeftAngle() {
        return servoLeft;
    }

    public int getRightAngle() {
        return servoRight;
    }

    public void setAutoTurn(boolean autoTurn) {
        this.autoTurn = autoTurn;
    }

    public void setCloseValue(int closeValue) {
        this.closeValue = closeValue;
    }

    public void setLightValue(int lightValue) {
        this.lightValue = lightValue;
    }

    public void setOpenValue(int openValue) {
        this.openValue = openValue;
    }

    public void setServoLeft(int servoLeft) {
        this.servoLeft = servoLeft;
    }

    public void setServoRight(int servoRight) {
        this.servoRight = servoRight;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getNetworkPassword() {
        return networkPassword;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public void setNetworkPassword(String networkPassword) {
        this.networkPassword = networkPassword;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType(){
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("\n Name :: %s \n Light value :: %d \n Servo left :: %d \n Servo right :: %d \n Open value :: %d \n Close value :: %d \n Auto turn :: %b \n",
                name, lightValue, servoLeft, servoRight, openValue, closeValue, autoTurn);
    }
}
