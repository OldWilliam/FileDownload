package me.wx.demo.mutithreadfiledowload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.wx.entities.FileInfo;

public class MainActivity extends AppCompatActivity {

    private TextView mFileNameTv = null;
    private ProgressBar mPb = null;
    private Button startBt = null;
    private Button stopBt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("断点续传");
        //初始化组件
        mFileNameTv = (TextView) findViewById(R.id.tv);
        mPb = (ProgressBar) findViewById(R.id.pb);
        startBt = (Button) findViewById(R.id.start);
        stopBt = (Button) findViewById(R.id.stop);
        mPb.setMax(100);
        //创建文件信息
        final FileInfo fileInfo = new FileInfo(0, "https://dl.wandoujia.com/files/jupiter/latest/wandoujia-wandoujia_web.apk", "豌豆荚.apk", 0, 0);
        //开始下载
        startBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileNameTv.setText(fileInfo.getFileName());
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
        //暂停下载
        stopBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReciver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReciver);
    }

    BroadcastReceiver mReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished =  intent.getIntExtra("finished", 0);
                mPb.setProgress(finished);
                if (finished == 100) {
                    mFileNameTv.setText("下载完成！");
                }
            }
        }
    };
}
