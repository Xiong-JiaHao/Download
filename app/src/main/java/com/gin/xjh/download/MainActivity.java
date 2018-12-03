package com.gin.xjh.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISH);
        registerReceiver(mReceiver,filter);

        mAdapter = new FileListAdapter(this, mFileList);
        mLvFile.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * 更新进度条的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DownloadService.ACTION_UPDATE.equals(intent.getAction())){
                int finished = intent.getIntExtra("finished",0);
                int id = intent.getIntExtra("id", 0);
                mAdapter.updateProgress(id, finished);
            } else if (DownloadService.ACTION_FINISH.equals(intent.getAction())) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                //更新进度为0
                mAdapter.updateProgress(fileInfo.getId(), fileInfo.getLength());
                Log.i("xxx", "" + fileInfo.getLength());
                Toast.makeText(context, mFileList.get(fileInfo.getId()).getFileName() + "下载完毕", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
