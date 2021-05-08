package com.example.mysmartroom;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mysmartroom.objects.DeviceAction;
import com.example.mysmartroom.objects.DeviceInfo;

import java.util.Arrays;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ControlDeviceActivity extends AppCompatActivity {

    private static String TAG = "ControlDeviceActivity";
    private String deviceIp;
    private String port;
    private Api api;
    private Context context;
    private TextView lightView, leftAngleView, rightAngleView;
    private EditText deviceNameEdit, networkNameEdit, networkPasswordEdit, openEdit, closeEdit, brightEdit;
    private androidx.appcompat.widget.Toolbar toolbar;
    private Switch autoTurnSwitch;

    private HashMap<String, String> deviceTypes = new HashMap<String, String>(){{
        put("{w}", "window");

    }};

    private Callback<DeviceInfo> basicResponse = new Callback<DeviceInfo>() {
        @Override
        public void onResponse(Call<DeviceInfo> call, Response<DeviceInfo> response) {
            if (response.isSuccessful()) {
                DeviceInfo device = response.body();
                Log.d(TAG, device.toString());
                deviceNameEdit.setText(device.getName());
                lightView.setText(String.valueOf(device.getLightValue()));
                leftAngleView.setText(String.valueOf(device.getLeftAngle()));
                rightAngleView.setText(String.valueOf(device.getRightAngle()));
                openEdit.setText(String.valueOf(device.getOpenValue()));
                closeEdit.setText(String.valueOf(device.getCloseValue()));
                brightEdit.setText(String.valueOf(device.getBrightValue()));
                autoTurnSwitch.setChecked(device.isAutoTurn());
                networkNameEdit.setText(device.getNetworkName());
                networkPasswordEdit.setText(device.getNetworkPassword());
                getSupportActionBar().setTitle("Connected to " + deviceTypes.get(device.getType()) + "-" + device.getName());
                Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
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

        deviceNameEdit = ((EditText) findViewById(R.id.device_name));
        lightView = (TextView) findViewById(R.id.light_value);
        leftAngleView = (TextView) findViewById(R.id.left_servo);
        rightAngleView = (TextView) findViewById(R.id.right_servo);
        openEdit = findViewById(R.id.open_value);
        closeEdit = findViewById(R.id.close_value);
        brightEdit = findViewById(R.id.bright_value);
        autoTurnSwitch = (Switch) findViewById(R.id.auto_turn);
        networkNameEdit = (EditText) findViewById(R.id.network_name);
        networkPasswordEdit = (EditText) findViewById(R.id.network_password);
        toolbar = findViewById(R.id.toolbar);

        autoTurnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_AUTO_TURN, "", autoTurnSwitch.isChecked() ? "on" : "off"))).enqueue(basicResponse);
            }
        });

        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_update:
                update();
        }
        return true;
    }

    private void update() {
        api.getDeviceInfo().enqueue(basicResponse);
    }

    public void openServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_OPEN, "","3"))).enqueue(basicResponse);
    }

    public void closeServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_CLOSE, "","3"))).enqueue(basicResponse);
    }

    public void middleServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_MIDDLE, "","3"))).enqueue(basicResponse);
    }

    public void openRightServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_OPEN, "","2"))).enqueue(basicResponse);
    }

    public void closeRightServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_CLOSE, "","2"))).enqueue(basicResponse);
    }

    public void middleRightServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_MIDDLE, "","2"))).enqueue(basicResponse);
    }

    public void openLeftServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_OPEN, "","1"))).enqueue(basicResponse);
    }

    public void closeLeftServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_CLOSE, "","1"))).enqueue(basicResponse);
    }

    public void middleLeftServo(View view) {
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_MIDDLE, "","1"))).enqueue(basicResponse);
    }

    public void openFullServo(View view){
        api.sendActions(Arrays.asList(new DeviceAction(DeviceAction.ACTION_OPEN_BRIGHT, "","3"))).enqueue(basicResponse);
    }

    public void restartDevice(View view){
        api.restart().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
        this.finish();
    }

    public void saveButton(View view) {
        String valueOpen = openEdit.getText().toString();
        String valueClose = closeEdit.getText().toString();
        String valueBright = brightEdit.getText().toString();
        String deviceName = deviceNameEdit.getText().toString();
        if (!valueOpen.isEmpty() && !valueClose.isEmpty() && !valueBright.isEmpty()) {
            api.sendActions(Arrays.asList(
                    new DeviceAction(DeviceAction.ACTION_SAVE_SETTINGS, DeviceAction.PARAM_BRIGHT, valueBright),
                    new DeviceAction(DeviceAction.ACTION_SAVE_SETTINGS, DeviceAction.PARAM_OPEN, valueOpen),
                    new DeviceAction(DeviceAction.ACTION_SAVE_SETTINGS, DeviceAction.PARAM_CLOSE, valueClose),
                    new DeviceAction(DeviceAction.ACTION_SAVE_SETTINGS, DeviceAction.PARAM_DEVICE_NAME,  deviceName),
                    new DeviceAction(DeviceAction.ACTION_SAVE_SETTINGS, DeviceAction.PARAM_NETWORK_NAME, networkNameEdit.getText().toString()),
                    new DeviceAction(DeviceAction.ACTION_SAVE_SETTINGS, DeviceAction.PARAM_NETWORK_PASSWORD, networkPasswordEdit.getText().toString())
            )).enqueue(basicResponse);
        }else{
            Toast.makeText(context, "Open/close/bright parameters can't be empty!", Toast.LENGTH_SHORT).show();
        }
    }
}
