package bluesea.ren.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/12.
 */
public class BluetoothServerService extends Service{
    @Override
    public IBinder onBind(Intent intent) {        return null;    }
    //蓝牙适配器
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //蓝牙通信线程
    private BluetoothCommunThread communThread;

    //控制信息广播器
    private BroadcastReceiver controlReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothTools.ACTION_START_DISCOVERY.equals(action)){
                //开始搜索
                bluetoothAdapter.enable();//打开蓝牙
                //开启蓝牙发现功能（300s）
                Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
                discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(discoveryIntent);
                //开启服务器广播
            }else if(BluetoothTools.ACTION_START_SERVICE.equals(action)){
                //开启后台连接蓝牙设备线程
                new BluetoothServerConnThread(serviceHandler).start();
            }else if(BluetoothTools.ACTION_DATA_TO_CLIENT.equals(action)){
                //获取数据
                Object data = intent.getSerializableExtra(BluetoothTools.DATA);
                if(communThread!=null){
                    communThread.writeObject(data);
                    System.out.println("向客户端发送数据");
                }
            }
            else if(BluetoothTools.ACTION_STOP_SERVICE.equals(action)){

                stopSelf();
            }

        }
    };
    //接收其他线程信息的Handler
    private Handler serviceHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what) {
                case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
                    //连接成功
                    //开启通信线程
                    System.out.println("开启通信线程");
                    communThread = new BluetoothCommunThread(serviceHandler, (BluetoothSocket)msg.obj);
                    new Thread(new Runnable() {
                        @Override
                        public void run () {

                            communThread.start();
                            //发送连接成功广播
                            Intent succIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
                            sendBroadcast(succIntent);
                        }
                    }).start();


                    //发送连接成功信息
                    Intent connSuccIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
                    sendBroadcast(connSuccIntent);
                    break;
                case BluetoothTools.MESSAGE_CONNECT_ERROR:
                    //连接错误
                    //发送连接错误广播
                    Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
                    sendBroadcast(errorIntent);
                    break;
                case BluetoothTools.MESSAGE_READ_OBJECT:
                    //读取到数据
                    //发送数据广播
                    System.out.println("接收到数据");
                    Intent dataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
                    dataIntent.putExtra(BluetoothTools.DATA, (Serializable) msg.obj);
                    sendBroadcast(dataIntent);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //获取通信线程
    public BluetoothCommunThread getBluetoothCommunThread(){
        return communThread;
    }
    @Override
    public void onCreate(){

        //controlRecever的IntentFilter
        IntentFilter controlFilter = new IntentFilter();
        controlFilter.addAction(BluetoothTools.ACTION_START_DISCOVERY);
        controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
        controlFilter.addAction(BluetoothTools.ACTION_START_SERVICE);
        controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_CLIENT);
        //注册BroadcastReceiver
        registerReceiver(controlReceiver,controlFilter);

        super.onCreate();
    }
//Service销毁时的回调函数

    @Override
    public void onDestroy() {
        //解除绑定
        if(communThread != null){
            communThread.isRun = false;
        }
        unregisterReceiver(controlReceiver);
        super.onDestroy();
    }

}
