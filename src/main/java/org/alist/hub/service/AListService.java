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
     * 启动Nginx
     */
    void startNginx();

    /**
     * 停止Nginx
     */
    void stopNginx();

    /**
     * 更新小雅
     */
    void update();

    /**
     * 更新前的准备
     * @return boolean
     */
    boolean checkUpdate();

    void updateTvBox(String site);
}
