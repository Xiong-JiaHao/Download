package com.gin.xjh.download.services;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.gin.xjh.download.db.ThreadDAO;
import com.gin.xjh.download.db.ThreadDAOImpl;
import com.gin.xjh.download.entities.FileInfo;
import com.gin.xjh.download.entities.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下载任务类
 */

public class DownloadTask {

    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDao;
    private int mFinished;
    public boolean isPause = false;
    private int mThreadCount = 1;//单个任务下载线程数
    private List<DownloadThread> mThreadList;
    public static ExecutorService sExecutorService = Executors.newCachedThreadPool();
    private Messenger mMessenger = null;

    private Timer mTimer = new Timer();//定时器

    public DownloadTask(Context mContext, FileInfo mFileInfo, Messenger mMessenger) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mMessenger = mMessenger;
        mFinished = 0;
        mDao = new ThreadDAOImpl(mContext);
    }

    public DownloadTask(Context mContext, FileInfo mFileInfo, Messenger mMessenger, int mThreadCount) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mMessenger = mMessenger;
        this.mThreadCount = mThreadCount;
        mFinished = 0;
        mDao = new ThreadDAOImpl(mContext);
    }

    public void download() {
        //读取数据库中的线程信息
        List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threads.size() == 0) {
            //初始化线程信息对象
            int len = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), len * i, len * (i + 1) - 1, 0);
                if (i == mThreadCount - 1) {
                    threadInfo.setEnds(mFileInfo.getLength());
                }
                threads.add(threadInfo);
                mDao.insertThread(threadInfo);
            }
        } else {
            for (ThreadInfo thread : threads) {
                mFinished += thread.getFinished();
            }
        }

        //启动定时任务
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = DownloadService.MSG_UPDATE;
                msg.arg1 = mFinished * 100 / mFileInfo.getLength();
                msg.arg2 = mFileInfo.getId();
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 300);

        //创建多个子线程进行下载
        mThreadList = new ArrayList<>();
        for (ThreadInfo info : threads) {
            DownloadThread thread = new DownloadThread(info);
            DownloadTask.sExecutorService.execute(thread);
            //添加线程到集合中
            mThreadList.add(thread);
        }
    }

    /**
     * 判断是否所有线程都执行完毕
     */
    private synchronized void checkAllThreadsFinished() {
        boolean allFinished = true;
        for (DownloadThread thread : mThreadList) {
            if (!thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            mTimer.cancel();
            //删除线程信息
            mDao.deleteThread(mFileInfo.getUrl());
            //通知UI下载结束
            Message msg = new Message();
            msg.what = DownloadService.MSG_FINISH;
            msg.obj = mFileInfo;
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;
        private boolean isFinished = false;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            //设置下载位置
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnds());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                //开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //读取数据
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    while ((len = input.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, len);
                        //把下载进度发送广播给Activity
                        //整个文件的下载进度
                        mFinished += len;
                        //每个线程的下载进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        //在暂停时保存下载进度
                        if (isPause) {
                            mTimer.cancel();
                            mDao.updateThread(mFileInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                            return;
                        }
                    }
                    isFinished = true;
                    checkAllThreadsFinished();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                try {
                    raf.close();
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
