package bluesea.ren.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
/**
 * Created by Administrator on 2016/9/13.
 * 实现服务器端的数据接收和发送功能
 */
public class ServerActivity extends Activity {

    //广播接收器
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)){
                //连接成功
                serverStateTextView.setText("连接成功");
             }else if(BluetoothTools.ACTION_DATA_TO_GAME.equals(action)){
                //接收数据
                TransmitBean data = new TransmitBean();
                data.setMsg(intent.getExtras().getSerializable(BluetoothTools.DATA)+"");
                String msg = "from remote" + new Date()+"\r\n"+data.getMsg()+"\r\n";
               MessageTV.append(msg);
            }
        }
    };

    private TextView serverStateTextView;
    private TextView MessageTV;
    private TextView sendTV;
    private EditText SendEdt;
    private Button OpenBluetooth;
    private Button OpenServer;
    private Button sendBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverStateTextView=(TextView)findViewById(R.id.serverStateText);
        serverStateTextView.setMovementMethod(new ScrollingMovementMethod());

        MessageTV = (TextView)findViewById(R.id.getMessageTV);
        //实现TextView的滚动效果
        MessageTV.setMovementMethod(new ScrollingMovementMethod());
        sendTV = (TextView)findViewById(R.id.sendMessageTV);
        //实现TextView的滚动效果
        sendTV.setMovementMethod(new ScrollingMovementMethod());
        SendEdt = (EditText)findViewById(R.id.SendET);
        sendBtn = (Button)findViewById(R.id.SendBt);

        OpenBluetooth = (Button)findViewById(R.id.button);
        OpenServer =(Button)findViewById(R.id.OpenServerBtn);
        serverStateTextView.setText("正在连接..."+"\r");
        OpenBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSearchIntent = new Intent(BluetoothTools.ACTION_START_DISCOVERY);
                sendBroadcast(startSearchIntent);
             }
        });
        OpenServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSearchIntent = new Intent(BluetoothTools.ACTION_START_SERVICE);
                sendBroadcast(startSearchIntent);
                serverStateTextView.setText("开启服务器");
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送消息
                if("".equals(SendEdt.getText().toString().trim())){
                    Toast.makeText(ServerActivity.this,"输入不能为空",Toast.LENGTH_SHORT).show();
                }else{

                    //发送消息
                    TransmitBean data = new TransmitBean();
                    data.setMsg(SendEdt.getText().toString());

                    Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_CLIENT);
                    sendDataIntent.putExtra(BluetoothTools.DATA,data.getMsg());
                    sendTV.append(SendEdt.getText().toString());
                    sendBroadcast(sendDataIntent);
                    SendEdt.setText("");
                }
            }
        });

     }

    @Override
    public void onStart() {
//      开启后台service
        Intent startService = new Intent(ServerActivity.this,BluetoothServerService.class);
        startService(startService);
        //注册BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
        intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
        registerReceiver(broadcastReceiver,intentFilter);
        super.onStart();

    }

    @Override
    public void onStop() {
        //关闭后台Service
        Intent startService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
        sendBroadcast(startService);
        unregisterReceiver(broadcastReceiver);
        super.onStop();

    }
}

