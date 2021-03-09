package com.example.mysmartroom;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ControlDeviceActivity extends AppCompatActivity {

    private static String TAG = "ControlDeviceActivity";
    private String deviceIp;
    private String port;
    private Api api;
    private Context context;
    private TextView nameView, lightView, leftAngleView, rightAngleView, openView, closeView;
    private EditText angleEdit;
    private Switch autoTurnSwitch;

    private Callback<DeviceInfo> basicResponse = new Callback<DeviceInfo>() {
        @Override
        public void onResponse(Call<DeviceInfo> call, Response<DeviceInfo> response) {
            if (response.isSuccessful()) {
                DeviceInfo device = response.body();
                Log.d(TAG, device.toString());
                nameView.setText(device.getName());
                lightView.setText(String.valueOf(device.getLightValue()));
                leftAngleView.setText(String.valueOf(device.getLeftAngle()));
                rightAngleView.setText(String.valueOf(device.getRightAngle()));
                openView.setText(String.valueOf(device.getOpenValue()));
                closeView.setText(String.valueOf(device.getCloseValue()));
                autoTurnSwitch.setChecked(device.isAutoTurn());
            } else {
                Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<DeviceInfo> call, Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.control_device);
        deviceIp = getIntent().getStringExtra("targetIp");
        port = getIntent().getStringExtra("port");
        Log.d(TAG, "Device ip : " + deviceIp);
        if (deviceIp == null) {
            return;
        }
        api = ApiCreator.getApi("http://" + deviceIp + ":" + port + "/");

        nameView = ((TextView) findViewById(R.id.device_name));
        lightView = (TextView) findViewById(R.id.light_value);
        leftAngleView = (TextView) findViewById(R.id.left_servo);
        rightAngleView = (TextView) findViewById(R.id.right_servo);
        openView = (TextView) findViewById(R.id.open_value);
        closeView = (TextView) findViewById(R.id.close_value);
        autoTurnSwitch = (Switch) findViewById(R.id.auto_turn);
        angleEdit = (EditText) findViewById(R.id.open_value);

        autoTurnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSettings("", "", "", isChecked ? "on" : "off");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceInfo();
    }

    private void saveSettings (String openValue, String closeValue, String angleSet, String autoSet){
        api.saveSettings(openValue, closeValue, angleSet, autoSet).enqueue(basicResponse);
    }

    private void updateDeviceInfo() {
        api.getDeviceInfo().enqueue(basicResponse);
    }

    private void open(String servo) {
        api.open(servo).enqueue(basicResponse);
    }

    private void middle(String servo) {
        api.middle(servo).enqueue(basicResponse);
    }

    private void close(String servo) {
        api.close(servo).enqueue(basicResponse);
    }

    public void update(View view) {
        updateDeviceInfo();
    }

    public void openServo(View view) {
        open("");
    }

    public void closeServo(View view) {
        close("");
    }

    public void middleServo(View view) {
        middle("");
    }

    public void openRightServo(View view) {
        open("right");
    }

    public void closeRightServo(View view) {
        close("right");
    }

    public void middleRightServo(View view) {
        middle("right");
    }

    public void saveButton(View view) {
        String valueOpen = ((EditText) findViewById(R.id.open_value)).getText().toString();
        String valueClose = ((EditText) findViewById(R.id.close_value)).getText().toString();
        if (!valueOpen.isEmpty() && !valueClose.isEmpty()) {
            saveSettings(valueOpen, valueClose, "", "");
        }else{
            Toast.makeText(context, "Parameter can't be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setAngle(View view){
        String angle = angleEdit.getText().toString();
        if (!angle.isEmpty()) {
            saveSettings("", "", angle, "");
        }else{
            Toast.makeText(context, "Parameter can't be empty!", Toast.LENGTH_SHORT).show();
        }
    }
}
