package com.example.mysmartroom;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private TextView lightView, leftAngleView, rightAngleView, openView, closeView;
    private EditText angleEdit, deviceNameEdit, networkNameEdit, networkPasswordEdit;
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
                openView.setText(String.valueOf(device.getOpenValue()));
                closeView.setText(String.valueOf(device.getCloseValue()));
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
        openView = (TextView) findViewById(R.id.open_value);
        closeView = (TextView) findViewById(R.id.close_value);
        autoTurnSwitch = (Switch) findViewById(R.id.auto_turn);
        angleEdit = (EditText) findViewById(R.id.open_value);
        networkNameEdit = (EditText) findViewById(R.id.network_name);
        networkPasswordEdit = (EditText) findViewById(R.id.network_password);
        toolbar = findViewById(R.id.toolbar);

        autoTurnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings("", "", "-1", autoTurnSwitch.isChecked() ? "on" : "off", "", "", "");
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

    private void saveSettings (String openValue, String closeValue, String angleSet, String autoSet,
                               String deviceName, String networkName, String networkPassword){
        api.saveSettings(openValue, closeValue, angleSet, autoSet, deviceName, networkName, networkPassword).enqueue(basicResponse);
    }

    private void update() {
        api.getDeviceInfo().enqueue(basicResponse);
    }

    private void open(String servo) {
        api.open(servo).enqueue(basicResponse);
    }

    private void openFull(String servo) {
        api.openFull(servo).enqueue(basicResponse);
    }

    private void middle(String servo) {
        api.middle(servo).enqueue(basicResponse);
    }

    private void close(String servo) {
        api.close(servo).enqueue(basicResponse);
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

    public void openLeftServo(View view) {
        open("left");
    }

    public void closeLeftServo(View view) {
        close("left");
    }

    public void middleLeftServo(View view) {
        middle("left");
    }

    public void openFullServo(View view){openFull("");}

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
        String valueOpen = ((EditText) findViewById(R.id.open_value)).getText().toString();
        String valueClose = ((EditText) findViewById(R.id.close_value)).getText().toString();
        String deviceName = deviceNameEdit.getText().toString();
        if (!valueOpen.isEmpty() && !valueClose.isEmpty() && !deviceName.isEmpty()) {
            saveSettings(valueOpen, valueClose, "-1", "", deviceName, networkNameEdit.getText().toString(), networkPasswordEdit.getText().toString());
        }else{
            Toast.makeText(context, "Open/close parameters can't be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setAngle(View view){
        String angle = angleEdit.getText().toString();
        if (!angle.isEmpty()) {
            saveSettings("", "", angle, "", "", "", "");
        }else{
            Toast.makeText(context, "Angle parameter can't be empty!", Toast.LENGTH_SHORT).show();
        }
    }
}
