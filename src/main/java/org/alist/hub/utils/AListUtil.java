package org.alist.hub.utils;

import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AList基本命令 用于启动和停止服务
 */
@Slf4j
public class AListUtil {
    private final static String ALIST_PATH = Constants.WORK_DIR + "/alist";
    private final static String ALIST_LOG = Constants.WORK_DIR + "/data/log";

    /**
     * 启动Alist服务器
     *
     * @return 如果启动成功，返回true；否则返回false
     */
    public static boolean start() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(ALIST_PATH, "server", "--no-prefix");
        File dir = new File(ALIST_LOG);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outFile = new File(ALIST_LOG + "/alist.log");
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(outFile));
        builder.redirectError(ProcessBuilder.Redirect.appendTo(outFile));
        builder.directory(new File(Constants.WORK_DIR));
        try {
//            Process process = builder.start();//TODO 保存ID到数据库 进行反向查询alist状态
            builder.start();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 停止Alist服务器
     *
     * @return 停止是否成功
     */
    public static boolean stop() {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            File outFile = new File(ALIST_LOG);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(outFile));
            builder.redirectError(ProcessBuilder.Redirect.appendTo(outFile));
            builder.command("pkill", "-f", ALIST_PATH);
            builder.directory(new File(Constants.WORK_DIR));
            builder.start();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 重启方法
     * 停止服务，等待5秒，然后尝试重新启动
     *
     * @return 如果成功重启返回true，否则返回false
     */
    public static boolean restart() {
        try {
            stop();
            Thread.sleep(5000);
            return start();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取配置文件的路径
     *
     * @return 配置文件的路径
     */
    public static Path getConfig() {
        String path = Constants.WORK_DIR + "/data/config.json";
        Path pathObj = Paths.get(path);
        File configFile = pathObj.toFile();
        if (!configFile.exists()) {
            throw new IllegalStateException("配置文件不存在");
        }
        return pathObj;
    }
}
