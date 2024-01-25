package org.alist.hub.service;

public interface AListService {
    /**
     * 启动
     *
     * @return 如果成功启动返回true，否则返回false
     */
    boolean startAList();

    /**
     * 停止
     *
     * @return 如果成功停止返回true，否则返回false
     */
    boolean stopAList();

    /**
     * 初始化
     */
    void initialize(String password);

    /**
     * 启动Nginx
     *
     * @return 是否成功启动Nginx
     */
    boolean startNginx();

    /**
     * 停止Nginx
     *
     * @return 是否成功停止Nginx
     */
    boolean stopNginx();


}
