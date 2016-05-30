package com.zh.smarthome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Monitor;

/**
 * Created by Administrator on 2016/5/28 0028.
 */
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {


    private Monitor monitor;
    private TextView conn_type;
    private ImageButton btn_left;
    private ImageButton btn_top;
    private ImageButton btn_right;
    private ImageButton btn_bottom;
    private Camera camera;

    /**
     * 开启activity跳转界面
     *
     * @param context
     * @param name
     * @param uid
     * @param pwd
     */
    public static void jumpActivity(Context context, String name, String uid, String pwd) {

        Intent intent = new Intent(context, CameraActivity.class);

        intent.putExtra("name", name);
        intent.putExtra("uid", uid);
        intent.putExtra("pwd", pwd);

        context.startActivity(intent);

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Camera.CONNECTION_STATE_CONNECT_FAILED:// 连接失败
                    conn_type.setText("连接失败");
                    break;
                case Camera.CONNECTION_STATE_CONNECTED:// 连接成功
                    conn_type.setText("连接成功");
                    displayCamera();
                    break;
                case Camera.CONNECTION_STATE_CONNECTING://正在连接
                    conn_type.setText("正在连接");
                    break;
                case Camera.CONNECTION_STATE_DISCONNECTED:// 断开连接
                    conn_type.setText("断开连接");
                    break;
                case Camera.CONNECTION_STATE_TIMEOUT:// 连接超时
                    conn_type.setText("连接超时");
                    break;
                case Camera.CONNECTION_STATE_UNKNOWN_DEVICE:// 设备未知
                    conn_type.setText("设备未知");
                    break;
                case Camera.CONNECTION_STATE_UNSUPPORTED:// 不支持的设备
                    conn_type.setText("不支持的设备");
                    break;
                case Camera.CONNECTION_STATE_WRONG_PASSWORD:// 密码错误
                    conn_type.setText("密码错误");
                    break;

            }
        }
    };


    /**
     * 展示预览界面
     */
    private void displayCamera() {

        if (camera != null && camera.isChannelConnected(Camera.DEFAULT_AV_CHANNEL)) {
            //  连接成功  进行预览
            // 绑定摄像头
            monitor.attachCamera(camera, Camera.DEFAULT_AV_CHANNEL);

            // 摄像头 设置焦距
            monitor.setMaxZoom(1.0f);

            //开始预览
            //  参数2 : 是否允许拍照
            camera.startShow(Camera.DEFAULT_AV_CHANNEL, true);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (camera != null) {


            camera.stopShow(Camera.DEFAULT_AV_CHANNEL);

            monitor.deattachCamera();
            camera.stop(Camera.DEFAULT_AV_CHANNEL);

            camera.disconnect();
            camera.unregisterIOTCListener(iotcListener);
            camera = null;
        }
    }

    /**
     * 连接摄像头
     *
     * @param name
     * @param uid
     * @param psw
     */
    private void connect(String name, String uid, String psw) {

        //1  初始化摄像头
        Camera.init();// A/V 通道

        //2. 获取摄像头对象
        camera = new Camera();
// 指令监听

        camera.registerIOTCListener(iotcListener);

        //3.连接摄像头
        //  uid : 设备的唯一标识
        camera.connect(uid);
//参数１　：　ｉｎｔ　 连接的渠道号
        // 参数2  设备的名称 name
        // 参数3 :  密码  admin
        camera.start(Camera.DEFAULT_AV_CHANNEL, name, psw);


        //发送测试指令

        //参数:  指令的类型
        // 参数3 :  指令值
        byte[] b = new byte[4];
        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ
                , AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());


    }


    private void sendPTZCmd(byte ptz) {
        //                - 移动的方向
//                        - 移动速度
//                        - 多少触控点
//                        - 移动的范围
//                        - 辅助设备
//                        - 渠道号
        byte[] cmd = AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd.parseContent(
                ptz, (byte) 0, (byte) 0, (byte) 0, (byte) 0
                , (byte) Camera.DEFAULT_AV_CHANNEL);
        // 渠道号
        // 指令类型
        // ptz 指令
        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, cmd);
    }


    IRegisterIOTCListener iotcListener = new IRegisterIOTCListener() {


        /*获取 渠道的信息  */
        @Override
        public void receiveChannelInfo(Camera camera, int channel, int req) {
            handler.sendEmptyMessage(req);
        }

        /*获取设备的反馈信息*/
        @Override
        public void receiveFrameData(Camera camera, int i, Bitmap bitmap) {

        }

        /*获取设备的其他信息*/
        @Override
        public void receiveFrameInfo(Camera camera, int i, long l, int i1, int i2, int i3, int i4) {

        }

        /*获取指令发送后的  反馈数据*/
        @Override
        public void receiveIOCtrlData(Camera camera, int i, int i1, byte[] bytes) {

        }

        /*获取会话信息*/
        @Override
        public void receiveSessionInfo(Camera camera, int i) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        initView();

        Intent intent = getIntent();
       String name = intent.getStringExtra("name");
       String uid = intent.getStringExtra("uid");
       String psw = intent.getStringExtra("pwd");

        connect(name, uid, psw);
    }

    private void initView() {
        monitor = (Monitor) findViewById(R.id.monitor);
        conn_type = (TextView) findViewById(R.id.conn_type);
        btn_left = (ImageButton) findViewById(R.id.btn_left);
        btn_top = (ImageButton) findViewById(R.id.btn_top);
        btn_right = (ImageButton) findViewById(R.id.btn_right);
        btn_bottom = (ImageButton) findViewById(R.id.btn_bottom);

        btn_left.setOnClickListener(this);
        btn_top.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        btn_bottom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left: // PTZ
                sendPTZCmd((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_LEFT);
                break;
            case R.id.btn_top:
                sendPTZCmd((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_UP);
                break;
            case R.id.btn_right:
                sendPTZCmd((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_RIGHT);
                break;
            case R.id.btn_bottom:
                sendPTZCmd((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_DOWN);
                break;
        }
    }
}
