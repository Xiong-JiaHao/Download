package com.gin.xjh.download.entities;
/**
 * 线程下载信息
 */
public class ThreadInfo {
    private int id;//文件id
    private String url;//文件下载url
    private int start;//线程从哪里开始下载
    private int ends;//线程到哪里结束下载
    private int finished;//完成多少

    public ThreadInfo() {
        super();
    }

    public ThreadInfo(int id, String url, int start, int ends, int finished) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.ends = ends;
        this.finished = finished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnds() {
        return ends;
    }

    public void setEnds(int ends) {
        this.ends = ends;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }
}
