package org.alist.hub.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * AList基本命令 用于启动和停止服务
 */
@Slf4j
public class AListUtil {
    private final static String ALIST_PATH = "/opt/alist/alist";
    private final static String ALIST_LOG = "/opt/alist/data/log/alist.log";

    /**
     * 启动Alist服务器
     *
     * @return 如果启动成功，返回true；否则返回false
     */
    public static boolean start() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(ALIST_PATH, "server", "--no-prefix");
        File outFile = new File(ALIST_LOG);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(outFile));
        builder.redirectError(ProcessBuilder.Redirect.appendTo(outFile));
        builder.directory(new File("/opt/alist"));
        try {
            Process process = builder.start();//TODO 保存ID到数据库 进行反向查询alist状态
            return process.isAlive();
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * 停止Alist服务器
     *
     * @return 停止是否成功
     */
    public boolean stop() {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            File outFile = new File(ALIST_LOG);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(outFile));
            builder.redirectError(ProcessBuilder.Redirect.appendTo(outFile));
            builder.command("pkill", "-f", ALIST_PATH);
            builder.directory(new File("/opt/alist"));
            builder.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 重启方法
     * 停止服务，等待5秒，然后尝试重新启动
     *
     * @return 如果成功重启返回true，否则返回false
     */
    public boolean restart() {
        try {
            stop();
            Thread.sleep(5000);
            return start();
        } catch (InterruptedException e) {
            return false;
        }
    }

}
