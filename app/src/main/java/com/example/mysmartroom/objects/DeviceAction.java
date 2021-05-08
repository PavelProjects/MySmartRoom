package com.example.mysmartroom.objects;

public class DeviceAction {
    public static final String ACTION_OPEN_BRIGHT = "action_bright";          //блокировать яркий свет
    public static final String ACTION_OPEN = "action_open";                   //открыть
    public static final String ACTION_CLOSE = "action_close";                 //закрыть
    public static final String ACTION_MIDDLE = "action_middle";               //приоткрыть
    public static final String ACTION_AUTO_TURN = "action_auto_turn";         //Включить или выключить автоматику
    public static final String ACTION_SAVE_SETTINGS = "action_save";          //Сохарнить настройки

    public static final String PARAM_OPEN = "open";
    public static final String PARAM_CLOSE = "close";
    public static final String PARAM_BRIGHT = "bright";                        //Названия параметров настроек
    public static final String PARAM_DEVICE_NAME = "device_name";
    public static final String PARAM_NETWORK_NAME = "network_name";
    public static final String PARAM_NETWORK_PASSWORD = "network_password";

    private String action;
    private String type;
    private String value;

    public DeviceAction(String action, String type, String value) {
        this.action = action;
        this.type = type;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
