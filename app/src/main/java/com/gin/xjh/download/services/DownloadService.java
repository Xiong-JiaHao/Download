package com.gin.xjh.download.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_FINISH = "ACTION_FINISH";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final int MSG_INIT = 0;
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //从Activity中传来的数据
        FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
        if (ACTION_START.equals(intent.getAction())) {
            //Log.i("test", "Start:" + fileInfo.toString());
            //启动初始化线程
            DownloadTask.sExecutorService.execute(new InitThread(fileInfo));
        } else if (ACTION_STOP.equals(intent.getAction())) {
            //从集合中取出下载任务
            DownloadTask task = mTasks.get(fileInfo.getId());
            if (task != null) {
                task.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    //Log.i("test", "Init:" + fileInfo.getLength());
                    //启动下载任务
                    DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 3);
                    task.download();
                    mTasks.put(fileInfo.getId(), task);
                    break;
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
