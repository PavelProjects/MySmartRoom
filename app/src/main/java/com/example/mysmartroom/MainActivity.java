package com.example.mysmartroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

    private ListView devicesListView;
    private ArrayAdapter<Device> adapter;
    private List<Device> devicesList = new ArrayList<>();
    private GifImageView pepe;
    private Context context;
    private Handler postToList;
    private EditText portEdit;
    private androidx.appcompat.widget.Toolbar toolbar;
    private String port = "3257";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        devicesList.add(new Device("10.42.0.192", "poeblo"));
        setContentView(R.layout.activity_main);
        portEdit = (EditText) findViewById(R.id.port_edit);
        toolbar = findViewById(R.id.toolbar);
        pepe = findViewById(R.id.pepo);
        devicesListView = findViewById(R.id.device_list);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Choose device");

        portEdit.setText(port);
        adapter = new ArrayAdapter<Device>(this, android.R.layout.two_line_list_item, android.R.id.text1, devicesList){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final Device device = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(device.getName());
                ((TextView) view.findViewById(android.R.id.text2)).setText(device.getIp());
                return view;
            }
        };

        devicesListView.setAdapter(adapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String targetIp = devicesList.get(position).getIp();
                if(!targetIp.isEmpty()){
                    Intent intent = new Intent(context, ControlDeviceActivity.class);
                    intent.putExtra("targetIp", targetIp);
                    intent.putExtra("port", port);
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

    public static Future<Device> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(new Callable<Device>() {
            @Override public Device call() {
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    String name = socket.getInetAddress().getHostName();
                    socket.close();
                    return new Device(ip, name);
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
    public Device(String ip, String name){
        this.ip = ip;
        this.name = name;
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
}