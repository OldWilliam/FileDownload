package me.wx.demo.mutithreadfiledowload;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import me.wx.db.ThreadDao;
import me.wx.db.ThreadDaoImpl;
import me.wx.entities.FileInfo;
import me.wx.entities.ThreadInfo;

/**
 * Created by ein on 2016/8/4.
 */
public class DownloadTask {
    private static final String TAG = "DowloadTask";
    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDao mDao = null;
    private int mFinished = 0;
    private volatile boolean isPause = false;

    public DownloadTask(Context context, FileInfo mFileInfo) {
        this.mContext = context;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDaoImpl(context);
    }

    public void stop() {
        isPause = true;
    }

    public void download() {
        isPause = false;
        Log.d(TAG, "download: start");
        //读取数据库线程的信息
        List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size() == 0) {
            //初始化线程信息对象
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            threadInfo = threadInfos.get(0);
        }
        //创建子线程进行下载
        Log.d(TAG, "download: "+threadInfo.toString());
        new DownloadThread(threadInfo).start();
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: DownloadThread");
            //向数据库插入线程信息
            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);
            }
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
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                Log.d(TAG, "run: startNet");
                if (conn.getResponseCode() == 200 || conn.getResponseCode() == 206) {
                    //读取数据
                    Log.d(TAG, "run: redData");
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        Log.d(TAG, "run: finished="+mFinished);
                        //写入文件
                        raf.write(buffer, 0, len);
                        //下载进度发送广播给Activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            intent.setAction(DownloadService.ACTION_UPDATE);
                            mContext.sendBroadcast(intent);
                        }
                        //暂停下载时，保存进度
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }
                    //删除线程信息
                    mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                    mContext.sendBroadcast(intent.putExtra("finished",100));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (input != null) {
                        input.close();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
