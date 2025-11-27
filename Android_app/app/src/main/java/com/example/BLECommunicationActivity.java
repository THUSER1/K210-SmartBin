package com.example;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.example.databinding.ActivityMainBinding;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class BLECommunicationActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "hello";
    private Bundle bundle;
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static List<BluetoothGattService> listBluetoothGattService = new ArrayList<BluetoothGattService>();
    private static List<BluetoothGattCharacteristic> listBluetoothGattCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
    private String name;
    private String address;
    private String status = "未连接";

    private String rev_str = "";
    private EditText editText;
    private Button btn_send;
    private Button btnStart;
    private BlutoothBLEService mBlutoothBLEService;
    private BluetoothGattCharacteristic target_chara;
    private TextView text1, text2,tv_result;
    private Handler mHandler = new Handler();
    private TextToSpeech textToSpeech;  // 声明 TTS 变量
    private int origin_th=0;


    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    private SharedPreferences mSharedPreferences;//缓存

    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private String language = "zh_cn";//识别语言

    private String resultType = "json";//结果内容数据格式


    private class tempHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    String str;
                    str = msg.getData().getString("connect_state");
                    text1.setText("状态：" + str);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private tempHandler myHandler = new tempHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blecommunication);

        initView();

        bundle = getIntent().getExtras();   //MainActivity里的putextra传来的信息
        name = bundle.getString(EXTRAS_DEVICE_NAME);
        address = bundle.getString(EXTRAS_DEVICE_ADDRESS);


        Intent getServiceIntent = new Intent(this, BlutoothBLEService.class);
        bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // 初始化 TTS
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.CHINESE);
                }
            }
        });

        SpeechUtility.createUtility(BLECommunicationActivity.this, SpeechConstant.APPID + "=722ab29a");

        btnStart.setOnClickListener(v -> {
            if( null == mIat ){
                // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                showMsg( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
                return;
            }

            mIatResults.clear();//清除数据
            setParam(); // 设置参数
            mIatDialog.setListener(mRecognizerDialogListener);//设置监听
            mIatDialog.show();// 显示对话框
        });

        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(BLECommunicationActivity.this, mInitListener);
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(BLECommunicationActivity.this, mInitListener);
        mSharedPreferences = getSharedPreferences("ASR",
                Activity.MODE_PRIVATE);

    }

    private void initView() {
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        editText = findViewById(R.id.edit_text);
        btn_send = findViewById(R.id.btn_send);
        btnStart = findViewById(R.id.btn_start);
        tv_result= findViewById(R.id.tv_result);
        text1.setText("状态：" + status);

        btn_send.setOnClickListener(this);
    }

    //注册广播和IntentFilter
    @Override
    protected void onResume() {
        registerReceiver(mBroadCastReceiver, makeGattUpdateIntentFilter());
        if (mBlutoothBLEService != null) {
            Log.i(TAG, "onResume: 99999999999999");
            boolean res = mBlutoothBLEService.connect(address);
            Log.i(TAG, "onResume: " + res);
        }
        super.onResume();
    }

    //取消注册广播和IntentFilter
    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadCastReceiver);
        mBlutoothBLEService = null;
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    //设置广播接收 服务(BlutoothBLEService.class)传过来得信息
    BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionString = intent.getAction();
            if (BlutoothBLEService.ACTION_GATT_CONNECTED.equals(actionString)) {
                Log.i(TAG, "onReceive: " + name + " 连接成功");
                status = "已连接";
                updateConnectionState(status);
            } else if (BlutoothBLEService.ACTION_GATT_DISCONNECTED.equals(actionString)) {
                Log.i(TAG, "onReceive: " + name + " 断开连接");
                status = "未连接";
                updateConnectionState(status);
            } else if (BlutoothBLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(actionString)) {
                Log.i(TAG, "onReceive: 广播接收到服务");
                displayGattServices(mBlutoothBLEService.getDupportedGattServices());
            } else if (BlutoothBLEService.ACTION_DATA_AVAILABLE.equals(actionString)) {
                Log.i(TAG, "onReceive: 有数据");
                displayData(intent.getExtras().getString(BlutoothBLEService.EXTRA_DATA), intent);
            }
        }
    };


    //接收的数据
    public void displayData(String rev_string, Intent intent) {
        try {
            byte[] data = intent.getByteArrayExtra("BLE_BYTE_DATA");
            if (data == null || data.length != 6) {  // 确保数据长度为 6
                Log.i(TAG, "displayData: data is empty or incorrect length");
                return;
            }

            // 检查帧头和帧尾
            if ((data[0] & 0xFF) != 0xAA || (data[1] & 0xFF) != 0xBB || (data[5] & 0xFF) != 0x5B) {
                Log.i(TAG, "displayData: Frame header or footer is incorrect");
                return;
            }

            // 解析数据
            int x = data[2] & 0xFF;  // 物体横坐标
            int y = data[3] & 0xFF;  // 物体纵坐标
            int id = data[4] & 0xFF; // 物体 ID

            // 物体类型映射
            String objectType;
            switch (id) {
                case 1:
                    objectType = "纸板";
                    break;
                case 2:
                    objectType = "废纸";
                    break;
                case 3:
                    objectType = "瓶子";
                    break;
                case 4:
                    objectType = "电池";
                    break;
                default:
                    objectType = "未知";
                    break;
            }

            // 生成最终字符串
            rev_string = String.format("物体的位置：（%d,%d） 物体类型：%s\n", x, y, objectType);
            // 语音播报“检测到 + 物体种类”
            final String speakText = "检测到 " + objectType;
            runOnUiThread(() -> {
                text2.setText(rev_str);
                if (textToSpeech != null&&id!=origin_th) {
                    textToSpeech.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, null);
                    origin_th=id;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 追加到 rev_str，确保新数据不会覆盖旧数据
        rev_str = rev_str + rev_string;

        // 更新 UI 显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text2.setText(rev_str);
            }
        });
    }


    //服务  找到特定的特征值进行读取数据并启用该特征值的通知
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        for (BluetoothGattService service : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
            for (final BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                if (characteristic.getUuid().toString().equals(CHARACTERISTIC_UUID)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBlutoothBLEService.readCharacteristic(characteristic);
                        }
                    }, 200);  //表示延迟200ms执行
                    mBlutoothBLEService.setCharacteristicNotification(characteristic, true);
                    target_chara = characteristic;
                }
//                List<BluetoothGattDescriptor> gattDescriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : gattDescriptors) {
//                    mBlutoothBLEService.getCharacteristicDescriptor(descriptor);
//                }
            }
        }
    }


    //IntentFilter 设置过滤 与广播进行注册
    public IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BlutoothBLEService.ACTION_GATT_CONNECTED);
        filter.addAction(BlutoothBLEService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BlutoothBLEService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BlutoothBLEService.ACTION_DATA_AVAILABLE);
        return filter;
    }

    //服务(BlutoothBLEService.class)连接 回调函数
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBlutoothBLEService = ((BlutoothBLEService.LocalBinder) service).getService();
            if (!mBlutoothBLEService.initialize()) {
                Log.i(TAG, "onServiceConnected: MainActivity BLE not init");
                finish();
            }
            Log.i(TAG, "onServiceConnected: 8888888888888");
            mBlutoothBLEService.connect(address);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBlutoothBLEService = null;
        }
    };

    //更新蓝牙连接状态
    private void updateConnectionState(String Status) {
        Message msg = new Message();
        msg.what = 1;
        Bundle bundle = new Bundle();
        bundle.putString("connect_state", status);
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    //数据分包处理
    private int[] dataSparate(int len) {
        int[] lens = new int[2];
        lens[0] = len / 20;
        lens[1] = len % 20;
        return lens;
    }


    //发送数据线程
    public class sendDataThread implements Runnable {

        public sendDataThread() {
            new Thread(this).start();
        }

        @Override
        public void run() {
            if (editText.getText() != null) {
                byte[] buff = null;
                try {
                    buff = editText.getText().toString().getBytes("GB2312");
                    Log.i(TAG, "run: " + buff.length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                int[] sendDataLens = dataSparate(buff.length);
                for (int i = 0; i < sendDataLens[0]; i++) {
                    byte[] data20 = new byte[20];
                    for (int j = 0; j < 20; j++) {
                        data20[j] = buff[i * 20 + j];
                    }
                    target_chara.setValue(data20);
                    mBlutoothBLEService.writeCharacteristic(target_chara);
                }
                if (sendDataLens[1] != 0) {
                    byte[] lastData = new byte[sendDataLens[1]];
                    for (int i = 0; i < sendDataLens[1]; i++) {
                        lastData[i] = buff[sendDataLens[0] * 20 + i];
                    }
                    if (lastData != null) {
                        target_chara.setValue(lastData);
                        mBlutoothBLEService.writeCharacteristic(target_chara);
                    } else {
                        Log.i(TAG, "run: last是空的");
                    }

                }

            }

        }
    }

    //发送数据实现点击事件
    @Override
    public void onClick(View v) {
        new sendDataThread();
    }

    //重写触摸事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                Log.i(TAG, "dispatchTouchEvent: 隐藏");
                hideSoftInput(view);
            }


        }

        return super.dispatchTouchEvent(ev);
    }

    public boolean isHideInput(View view, MotionEvent ev) {
        if (view != null && (view instanceof EditText)) {
            int[] location = new int[2];
            view.getLocationInWindow(location);
            int left = location[0];
            int top = location[1];
            int right = left + view.getWidth() + btn_send.getWidth();
            int bottom = top + view.getHeight() + btn_send.getHeight();
            int y = (int) ev.getY() - 850;
            if (ev.getX() > left && ev.getX() < right && y > top && y < bottom) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }

    }


    public void hideSoftInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            Log.i("touch", "Hide SoftInput");
        } else {
            Log.i("touch", "hideSoftInput: view is null, skipping hideSoftInput");
        }
    }


    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            showMsg("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    };

    /**
     * 听写UI监听器
     */
    private final RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);//结果数据解析
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showMsg(error.getPlainDescription(true));
        }
    };

    /**
     * 提示消息
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(BLECommunicationActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 数据解析
     *
     * @param results
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        tv_result.setText(resultBuffer.toString());//听写结果显示

        handleVoiceCommand(text); // 处理语音指令
    }
    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + language);// 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {

            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private void handleVoiceCommand(String command) {
        byte[] dataFrame = new byte[4]; // 存储发送的数据帧
        dataFrame[0] = (byte) 0xCC; // 帧头
        dataFrame[3] = (byte) 0xDD; // 帧尾
        String dis_string="";
        // 解析控制指令
        if (command.contains("开")&&command.contains("机")) {
            dataFrame[1] = 0x00; // 开启舵机
            dataFrame[2] = 0x00; // 无垃圾种类纠正
            Log.i(TAG, "成功开启！！");
            dis_string=String.format("指令识别：开启舵机\n");
            rev_str = rev_str + dis_string;
            runOnUiThread(() -> text2.setText(rev_str));
            textToSpeech.speak("已开启舵机", TextToSpeech.QUEUE_FLUSH, null, null);
        } else if (command.contains("关")&&command.contains("机")) {
            dataFrame[1] = 0x01; // 关闭舵机
            dataFrame[2] = 0x00; // 无垃圾种类纠正
            Log.i(TAG, "成功关闭！！");
            dis_string=String.format("指令识别：关闭舵机\n");
            rev_str = rev_str + dis_string;
            runOnUiThread(() -> text2.setText(rev_str));
            textToSpeech.speak("已关闭舵机", TextToSpeech.QUEUE_FLUSH, null, null);
        } else if (command.contains("是")) {
            dataFrame[1] = 0x00; // 不影响舵机，仅用于纠正垃圾类型
            dataFrame[2] = getTrashType(command);
        } else {
            Log.d("VoiceCommand", "未识别的指令：" + command);
            return; // 退出，不发送数据
        }

        sendToSTM32(dataFrame); // 发送数据到STM32
    }

    // 解析垃圾种类
    private byte getTrashType(String command) {
        if (command.contains("纸板")) return 0x01;
        if (command.contains("废纸")||command.contains("纸巾")) return 0x02;
        if (command.contains("瓶")) return 0x03;
        if (command.contains("电池")) return 0x04;
        return 0x00; // 未识别，保持默认
    }

    // 发送数据到BLE模块
    private void sendToSTM32(byte[] data) {
        if (target_chara != null) {
            target_chara.setValue(data);
            mBlutoothBLEService.writeCharacteristic(target_chara);
            Log.d("BLE", "发送数据：" + Arrays.toString(data));
        } else {
            Log.e("BLE", "蓝牙未连接或特征值为空");
        }
        Log.i("BLE", "发送数据：" + Arrays.toString(data));
    }


}

