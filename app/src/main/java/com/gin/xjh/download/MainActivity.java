package com.gin.xjh.download;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.gin.xjh.download.entities.FileInfo;
import com.gin.xjh.download.services.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mLvFile;
    private List<FileInfo> mFileList;
    private FileListAdapter mAdapter;
    private Messenger mServiceMessenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }
    private void initView() {
        mLvFile = findViewById(R.id.lvFile);
    }


    private void initEvent() {
        mFileList = new ArrayList<>();
        FileInfo fileInfo0 = new FileInfo(0, "http://music.163.com/" +
                "song/media/outer/url?id=557581647.mp3", "一眼一生", 4806156, 0);//l
        FileInfo fileInfo1 = new FileInfo(1, "http://music.163.com/" +
                "song/media/outer/url?id=1313897867.mp3", "不负时代", 2975495, 0);
        FileInfo fileInfo2 = new FileInfo(2, "http://music.163.com/" +
                "song/media/outer/url?id=1306386464.mp3", "万王归来", 5232475, 0);
        FileInfo fileInfo3 = new FileInfo(3, "http://music.163.com/" +
                "song/media/outer/url?id=531295350.mp3", "越清醒越孤独", 5004687, 0);

        mFileList.add(fileInfo0);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);


        mAdapter = new FileListAdapter(this, mFileList);
        mLvFile.setAdapter(mAdapter);

        //绑定Service
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //获得Service中的Messenger
            mServiceMessenger = new Messenger(iBinder);
            //设置适配器中的Messenger
            mAdapter.setmMessenger(mServiceMessenger);
            //创建Activity中的Messenger
            Messenger messenger = new Messenger(mHandler);
            //创建消息
            Message msg = new Message();
            msg.what = DownloadService.MSG_BIND;
            msg.replyTo = messenger;
            //使用Service的Messenger发送Activity中的Messenger
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadService.MSG_UPDATE:
                    int finished = msg.arg1;
                    int id = msg.arg2;
                    mAdapter.updateProgress(id, finished);
                    break;
                case DownloadService.MSG_FINISH:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    //更新进度为0
                    mAdapter.updateProgress(fileInfo.getId(), fileInfo.getLength());
                    Toast.makeText(MainActivity.this, mFileList.get(fileInfo.getId()).getFileName() + "下载完毕", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
