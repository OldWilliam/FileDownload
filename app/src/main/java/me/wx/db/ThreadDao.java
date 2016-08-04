package me.wx.db;

import java.util.List;

import me.wx.entities.ThreadInfo;

/**
 * Created by ein on 2016/8/4.
 * 数据访问接口
 */
public interface ThreadDao {
    /**
     * 插入线程信息
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程信息
     * @param url
     * @param thread_id
     */
    public void deleteThread(String url, int thread_id);

    /**
     * 更新线程下载进度
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThread(String url, int thread_id, int finished);

    /**
     * 查询文件线程信息
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 判断线程信息是否存在
     * @param url
     * @param thrad_info
     * @return
     */
    public boolean isExists(String url, int thread_info);
}
