package com.example;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.print.PrinterId;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public class BlutoothBLEService extends Service {


    private final static String TAG = "hello";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    private final static int STATE_DISCONNECTED = 0;
    private final static int STATE_CONNECTING = 1;
    private final static int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.EXTRA_DATA";

    private final IBinder mBinder = new LocalBinder();


    //连接外围设备的回调函数
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        //重写 蓝牙连接状态
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    mBluetoothGatt.discoverServices();   //发现服务
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    broadcastUpdate(intentAction);
                }
            }
        }

        //重写 蓝牙发现服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.i(TAG, "onServicesDiscovered: 蓝牙发现服务");
            } else {
                Log.i(TAG, "onServicesDiscovered: 蓝牙发现服务失败" + status);
            }

        }

        //重写 蓝牙读特征
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicRead: is called");
                byte[] sucString = characteristic.getValue();
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        //重写 蓝牙写特征
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicWrite: 写数据成功");
        }

        //重写 蓝牙特征改变
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged: changed changed changed changed changed ");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        //重写 蓝牙读描述值
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onDescriptorRead: Read Read Read");
                byte[] desc = descriptor.getValue();
                if (desc == null) {
                    Log.i(TAG, "onDescriptorRead: desc is null null null");
                }
            }
        }

        //重写 蓝牙写描述值
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onDescriptorWrite: Write Write Write");
            }
        }


        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onReliableWriteCompleted: onReliableWriteCompleted onReliableWriteCompleted onReliableWriteCompleted");
            }
        }

        //重写 获取蓝牙信号值
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onReliableWriteCompleted: RSSI RSSI RSSI");
                broadcastUpdate(ACTION_DATA_AVAILABLE, rssi);
            }
        }
    };


    //更新广播
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    //更新广播
    private void broadcastUpdate(final String action, int rssi) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, rssi);
        sendBroadcast(intent);
    }

    //更新广播
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        //将data的数据传输给主空间中保存
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                int unsignedByte = byteChar & 0xFF;  // 转换为0-255的无符号值
                //stringBuilder.append(String.format("%02X", byteChar));  //原格式
                stringBuilder.append(String.format("%d", unsignedByte)); // 使用十进制格式
                //Log.i(TAG, "broadcastUpdate: byteChar is:" + byteChar); //原打印
                Log.i(TAG, "broadcastUpdate: byteChar is:" + unsignedByte);
            }
            /*intent.putExtra("BLE_BYTE_DATA", data);
            intent.putExtra(EXTRA_DATA, new String(data));*/
            intent.putExtra("BLE_BYTE_DATA", data);
            intent.putExtra(EXTRA_DATA, stringBuilder.toString()); // 传输转换后的十进制数据

        }
        sendBroadcast(intent);

    }

    //蓝牙连接外围设备
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.i(TAG, "connect: BLE not init");
            return false;
        }
        if (mBluetoothDeviceAddress != null && mBluetoothGatt != null && mBluetoothDeviceAddress.equals(address)) {
            Log.i(TAG, "connect: Trying to use an existing mBluetoothGatt for connection");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.i(TAG, "connect: device not found");
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "connect: Trying to create a connection");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    //取消连接
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "disconnect: BLE not init");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    //关闭所有蓝牙连接
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //读特征值
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "readCharacteristic: BLE not init");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //写入特征值
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "writeCharacteristic: BLE not init");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    //读取RSSI
    public void readRssi() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "readRssi: BLE not init");
            return;
        }
        mBluetoothGatt.readRemoteRssi();
    }

    //设置特征值变化通知
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristicNotification, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "setCharacteristicNotification: BLE not init");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristicNotification, enabled);
        BluetoothGattDescriptor descriptor = characteristicNotification.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    //获取特征值下的描述值
    public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "getCharacteristicDescriptor: BLE not init");
            return;
        }
        mBluetoothGatt.readDescriptor(descriptor);
    }

    //获取已配对蓝牙的所有服务
    public List<BluetoothGattService> getDupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

    //蓝牙初始化 在ServiceConnection中调用
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.i(TAG, "initialize: mBluetoothManager 初始化失败");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "initialize: mBluetoothAdapter 初始化失败");
            return false;
        }
        return true;
    }


    public class LocalBinder extends Binder {
        public BlutoothBLEService getService() {
            return BlutoothBLEService.this;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }


}
