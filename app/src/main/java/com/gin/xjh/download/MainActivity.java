package com.gin.xjh.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gin.xjh.download.entities.FileInfo;
import com.gin.xjh.download.services.DownloadService;

public class MainActivity extends AppCompatActivity {

    private TextView mTvFileName;
    private ProgressBar mPbProgress;
    private Button mBtStop, mBtStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }
    private void initView() {
        mTvFileName = findViewById(R.id.tvFileName);
        mPbProgress = findViewById(R.id.pbProgress);
        mBtStop = findViewById(R.id.btStop);
        mBtStart = findViewById(R.id.btStart);
    }


    private void initEvent() {
        final FileInfo fileInfo = new FileInfo(0, "http://music.163.com/" +
                "song/media/outer/url?id=557581647.mp3", "一眼一生", 0, 0);
        mBtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
        mBtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
        mPbProgress.setMax(100);
        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver,filter);
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
                mPbProgress.setProgress(finished);
            }
        }
    };
}
