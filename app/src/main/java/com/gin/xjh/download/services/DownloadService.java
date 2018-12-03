package com.gin.xjh.download.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;

import com.gin.xjh.download.entities.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadService extends Service {

    public static final String DOWNLOAD_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/downloads/";
    public static final int MSG_INIT = 0x1;
    public static final int MSG_BIND = 0x2;
    public static final int MSG_START = 0x3;
    public static final int MSG_STOP = 0x4;
    public static final int MSG_FINISH = 0x5;
    public static final int MSG_UPDATE = 0x6;
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();

    private Messenger mActivityMessenger = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //创建一个Messenger对象，包含Handler的引用
        Messenger messenger = new Messenger(mHandler);
        return messenger.getBinder();
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FileInfo fileInfo;
            DownloadTask task;
            switch (msg.what) {
                case MSG_INIT:
                    fileInfo = (FileInfo) msg.obj;
                    //Log.i("test", "Init:" + fileInfo.getLength());
                    //启动下载任务
                    task = new DownloadTask(DownloadService.this, fileInfo, mActivityMessenger, 3);
                    task.download();
                    mTasks.put(fileInfo.getId(), task);
                    break;
                case MSG_BIND:
                    mActivityMessenger = msg.replyTo;
                    break;
                case MSG_START:
                    fileInfo = (FileInfo) msg.obj;
                    DownloadTask.sExecutorService.execute(new InitThread(fileInfo));
                    break;
                case MSG_STOP:
                    fileInfo = (FileInfo) msg.obj;
                    task = mTasks.get(fileInfo.getId());
                    if (task != null) {
                        task.isPause = true;
                    }
            }
        }
    };
    class InitThread extends Thread {
        private FileInfo mFileInfo = null;

        public InitThread(FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                //连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                int length = -1;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //获得文件长度
                    length = conn.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                //在本地创建文件
                File dir = new File(DOWNLOAD_PATH);//验证下载地址
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");//r：读权限，w：写权限，d：删除权限
                //设置文件长度
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
