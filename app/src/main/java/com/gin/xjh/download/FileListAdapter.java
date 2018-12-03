package com.gin.xjh.download;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gin.xjh.download.entities.FileInfo;
import com.gin.xjh.download.services.DownloadService;

import java.util.List;

public class FileListAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileInfo> mFileList;
    private Messenger mMessenger = null;

    public FileListAdapter(Context mContext, List<FileInfo> mFileList) {
        this.mContext = mContext;
        this.mFileList = mFileList;
    }

    public void setmMessenger(Messenger mMessenger) {
        this.mMessenger = mMessenger;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int i) {
        return mFileList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        final FileInfo fileInfo = mFileList.get(i);
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem, null);
            holder = new ViewHolder();
            holder.btStart = view.findViewById(R.id.btStart);
            holder.btStop = view.findViewById(R.id.btStop);
            holder.pbFile = view.findViewById(R.id.pbProgress);
            holder.tvFile = view.findViewById(R.id.tvFileName);
            //设置控件
            holder.tvFile.setText(fileInfo.getFileName());
            holder.pbFile.setMax(100);
            holder.btStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Message msg = new Message();
                    msg.what = DownloadService.MSG_START;
                    msg.obj = fileInfo;
                    try {
                        mMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
            holder.btStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Message msg = new Message();
                    msg.what = DownloadService.MSG_STOP;
                    msg.obj = fileInfo;
                    try {
                        mMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.pbFile.setProgress(fileInfo.getFinished());
        return view;
    }

    public void updateProgress(int id, int progress) {
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        private TextView tvFile;
        private Button btStart, btStop;
        private ProgressBar pbFile;

    }
}
