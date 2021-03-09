package com.example.mysmartroom;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

    private ListView devicesListView;
    private ArrayAdapter<String> adapter;
    private List<String> devicesList = new ArrayList<>();
    private GifImageView pepe;
    private Context context;
    private Handler postToList;
    private int port = 3257;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        devicesList.add("10.42.0.192");
        setContentView(R.layout.activity_main);
        pepe = findViewById(R.id.pepo);
        devicesListView = findViewById(R.id.device_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        devicesListView.setAdapter(adapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String targetIp = devicesList.get(position);
                if(!targetIp.isEmpty()){
                    Intent intent = new Intent(context, ControlDeviceActivity.class);
                    intent.putExtra("targetIp", targetIp);
                    intent.putExtra("port", String.valueOf(port));
                    startActivity(intent);
                }
            }
        });
        postToList = new Handler(){
            public void handleMessage(android.os.Message msg){
                adapter.notifyDataSetChanged();
            }
        };
    }

    public void updateDevices(View view) {
        try {
            update();
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
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
                    final List<Future<String>> futures = new ArrayList<>();
                    for (int i = 1; i <= 193; i++) {
//                        Log.d(TAG, subnet + i);
                        futures.add(portIsOpen(es, subnet + i, port, timeout));
                    }
                    es.shutdown();
                    for (final Future<String> f : futures) {
                        if(f.get() != null){
                            Log.d(TAG, f.get());
                            devicesList.add(f.get());
                        }
                    }
                    postToList.sendEmptyMessage(0);
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                } finally {
                    pepe.setVisibility(View.INVISIBLE);
                }
            }
        });
        updateThread.start();
    }

    public static Future<String> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(new Callable<String>() {
            @Override public String call() {
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(ip, port), timeout); //todo надо как-то прокидывать имя устройства
                    socket.close();
                    return ip;
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