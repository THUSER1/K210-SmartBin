package com.example;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    final static String TAG = "hello";


    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BluetoothDevice> listdevices = new ArrayList<BluetoothDevice>();
    private List<String> listdevicename = new ArrayList<String>();

    private Handler mHanler = new Handler();
    private Spinner mSpinner;
    private ArrayAdapter<String> ListAdapter;

    private Button btn_Search, btn_On, btn_Off;
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPower();//获取位置权限
        initView();//初始化界面
        turnOnBle();//开启蓝牙
    }


   /* public void requestPower() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限.它在用户选择"不再询问"的情况下返回false
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }*/

    public void requestPower() {
        List<String> toApplyList = new ArrayList<>();

        // 需要申请的权限
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION // 新增位置权限
        };

        // 遍历检查已有权限
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }

        // Android 12+ 需要的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                toApplyList.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                toApplyList.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        // 申请权限
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(new String[0]), 123);
        }
   }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "缺少必要权限：" + permissions[i], Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    //初始化界面
    public void initView() {

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();


        btn_Search = findViewById(R.id.btn_Search);
        btn_On = findViewById(R.id.btn_On);
        btn_Off = findViewById(R.id.btn_Off);
        mSpinner = findViewById(R.id.spinner);


        btn_Search.setOnClickListener(this);
        btn_On.setOnClickListener(this);
        btn_Off.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {  //修改properties
        switch (v.getId()) {
            case R.id.btn_On:
                turnOnBle();
                break;
            case R.id.btn_Off:
                turnOffBle();
                break;
            case R.id.btn_Search:

                if (btn_Search.getText().equals("搜索设备")) {
                    listdevices.clear();
                    listdevicename.clear();
                    listdevicename.add("<BLE List>");
                    searchBle();
                } else if (btn_Search.getText().equals("停止搜索")) {
                    disSearchBle();
                }

                break;

        }
    }

    //开启蓝牙
    public void turnOnBle() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled() == false) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 11);
            } else {
                Toast.makeText(this, "蓝牙已经打开了", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        }

    }

    //关闭蓝牙
    public void turnOffBle() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        } else {
            Toast.makeText(this, "蓝牙已经关闭了", Toast.LENGTH_SHORT).show();
        }
    }

    //延时停止搜索线程
    Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            bluetoothLeScanner.stopScan(scanCallback);
            btn_Search.setText("搜索设备");
            setBLESpinner();
        }
    };

    //开启蓝牙扫描
    public void searchBle() {
        if (bluetoothAdapter.isEnabled()) {
            mHanler.postDelayed(myRunnable, 5000);
            btn_Search.setText("停止搜索");
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            Toast.makeText(MainActivity.this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
        }

    }

    //用户提前点按钮停止搜索
    public void disSearchBle() {
        if (bluetoothLeScanner != null) {
            mHanler.removeCallbacks(myRunnable);
            bluetoothLeScanner.stopScan(scanCallback);
            setBLESpinner();
            btn_Search.setText("搜索设备");
        }

    }

    //扫描蓝牙设备的回调
    public ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothDevice device = result.getDevice();
                    String str = device.getName();
                    if ((listdevicename.indexOf(str) == -1) && (device.getName() != null)) {  //新设备&&有名字
                        listdevices.add(device);
                        listdevicename.add(str);
                        Log.i(TAG, str);
                    }

                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    //选择蓝牙设备
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String Item = parent.getSelectedItem().toString();
        if (Item != "<BLE List>") {
            for (BluetoothDevice device : listdevices) {
                if (Item.equals(device.getName())) {
                    Intent intent = new Intent(this, BLECommunicationActivity.class);
                    intent.putExtra(EXTRAS_DEVICE_NAME, device.getName());
                    intent.putExtra(EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    startActivity(intent);

                }
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //将扫描到的设备添加到Spinner控件中
    public void setBLESpinner() {
        mSpinner.setOnItemSelectedListener(this);
        ListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listdevicename);
        ListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(ListAdapter);

    }
}

