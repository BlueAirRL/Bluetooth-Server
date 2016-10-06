package bluesea.ren.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Administrator on 2016/9/13.
 */
public class BluetoothServerConnThread extends Thread{
    private Handler serviceHandler;  //用于同Service通信的Handler
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;//用于通信的Socket
    private BluetoothServerSocket serverSocket;

    public BluetoothServerConnThread(Handler handler){
        this.serviceHandler = handler;
        adapter = BluetoothAdapter.getDefaultAdapter();
    }
    @Override
    public void run(){
        try{
            System.out.println("开启UUID");
            serverSocket=adapter.listenUsingRfcommWithServiceRecord("Server",BluetoothTools.PRIVATE_UUID);
            socket = serverSocket.accept();
        }catch(Exception e){
            //发送连接失败
            serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
            e.printStackTrace();
            return;
        }finally {
           try {
               serverSocket.close();
           }
           catch(Exception e){
               e.printStackTrace();
           }
        }

            if(socket!=null){
                System.out.println("接收数据");
                //发送连接成功消息
                Message msg = serviceHandler.obtainMessage();
                msg.what=BluetoothTools.MESSAGE_CONNECT_SUCCESS;
                msg.obj=socket;
                msg.sendToTarget();

            }else{
                //发送连接失败消息
                serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
                return;
            }
        }

    }


