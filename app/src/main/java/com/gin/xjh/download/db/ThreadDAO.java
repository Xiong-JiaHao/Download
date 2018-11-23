package com.gin.xjh.download.db;

import com.gin.xjh.download.entities.ThreadInfo;

import java.util.List;

/**
 * 数据访问接口
 */

public interface ThreadDAO {

    /**
     * 插入线程信息
     */
    void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程信息
     */
    void deleteThread(String url, int thread_id);

    /**
     * 更新线程信息
     */
    void updateThread(String url, int thread_id, int finished);

    /**
     * 查询文件的线程信息
     */
    List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     */
    boolean isExists(String url, int thread_id);
}
