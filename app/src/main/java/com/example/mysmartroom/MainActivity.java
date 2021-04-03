package com.example.mysmartroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pl.droidsonroids.gif.GifImageView;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {

    private static String TAG = "MAIN_MENU";

    private ListView devicesListView, savedDevicesListView;
    private ArrayAdapter<Device> adapter, savedAdapter;
    private List<Device> devicesList = new ArrayList<>();
    private List<Device> savedDevicesList = new ArrayList<>();
    private List<String> savedIps = new ArrayList<>();
    private GifImageView pepe;
    private Context context;
    private Handler updateSearchListHandler, updateSavedHandler;
    private EditText portEdit;
    private androidx.appcompat.widget.Toolbar toolbar;
    private String port = "3257";
    private AdapterView.OnItemClickListener basicClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Device device = ((Device) parent.getItemAtPosition(position));
            if(device.isAvailable()){
                Intent intent = new Intent(context, ControlDeviceActivity.class);
                intent.putExtra("targetIp", device.getIp());
                intent.putExtra("port", port);
                startActivity(intent);
            }else{
                Toast.makeText(context, "This device is not available!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        portEdit = (EditText) findViewById(R.id.port_edit);
        savedDevicesListView = findViewById(R.id.saved_device_list);
        toolbar = findViewById(R.id.toolbar);
        pepe = findViewById(R.id.pepo);
        devicesListView = findViewById(R.id.device_list);

        savedIps.add("10.42.0.192");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Choose device");

        portEdit.setText(port);

        adapter = getAdapterForList(devicesList);
        devicesListView.setAdapter(adapter);
        devicesListView.setOnItemClickListener(basicClickListener);

        savedAdapter = getAdapterForList(savedDevicesList);
        savedDevicesListView.setAdapter(savedAdapter);
        savedDevicesListView.setOnItemClickListener(basicClickListener);

        updateSearchListHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                adapter.notifyDataSetChanged();
            }
        };

        updateSavedHandler = new Handler(){
            public void handleMessage(Message message){
                savedAdapter.notifyDataSetChanged();
            }
        };
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
                updateSavedDevices();
                update();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSavedDevices();
    }

    private ArrayAdapter<Device> getAdapterForList(List<Device> list){
        return new ArrayAdapter<Device>(this, android.R.layout.two_line_list_item, android.R.id.text1, list){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final Device device = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(device.getName());
                ((TextView) view.findViewById(android.R.id.text2)).setText(device.getIp());
                view.setBackgroundColor(device.isAvailable() ? Color.GREEN : Color.RED);
                return view;
            }
        };
    }

    private void updateSavedDevices(){
        savedDevicesList.clear();
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                savedIps.forEach(ip -> {
                    Socket socket = new Socket();
                    Device device = new Device(ip, ip, false);
                    try {
                        socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 250);
                        device.setName(socket.getInetAddress().getHostName());
                        device.setAvailable(true);
                        socket.close();
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getLocalizedMessage());
                    }finally {
                        savedDevicesList.add(device);
                        updateSavedHandler.sendEmptyMessage(0);
                    }
                });
            }
        });
        updateThread.start();
    }

    private void update(){
        Toast.makeText(this, "Updating", Toast.LENGTH_SHORT).show();
        devicesList.clear();
        pepe.setVisibility(View.VISIBLE);
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    final ExecutorService es = Executors.newFixedThreadPool(20);
                    String subnet = getSubnetAddress(mWifiManager.getDhcpInfo().gateway);
                    Log.d(TAG, "Subnet : " + subnet);
                    final int timeout = 500;
                    final List<Future<Device>> futures = new ArrayList<>();
                    for (int i = 1; i <= 193; i++) {
//                        Log.d(TAG, subnet + i);
                        futures.add(portIsOpen(es, subnet + i, Integer.parseInt(port), timeout));
                    }
                    es.shutdown();
                    for (final Future<Device> f : futures) {
                        if(f.get() != null){
                            Log.d(TAG, f.get().getName());
                            devicesList.add(f.get());
                        }
                    }
                    updateSearchListHandler.sendEmptyMessage(0);
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                } finally {
                    pepe.setVisibility(View.INVISIBLE);
                }
            }
        });
        updateThread.start();
    }

    public static Future<Device> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(new Callable<Device>() {
            @Override public Device call() {
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    String name = socket.getInetAddress().getHostName();
                    socket.close();
                    return new Device(ip, name, true);
                } catch (Exception ex) {
                    return null;
                }
            }
        });
    }
    private String getSubnetAddress(int address){
        String ipString = String.format(
                "%d.%d.%d.",
                (address & 0xff),
                (address >> 8 & 0xff),
                (address >> 16 & 0xff));

        return ipString;
    }
}

class Device{
    private String ip;
    private String name;
    private boolean available;

    public Device(String ip, String name, boolean available){
        this.ip = ip;
        this.name = name;
        this.available = available;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}