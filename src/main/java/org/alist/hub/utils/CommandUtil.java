package org.alist.hub.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * AList基本命令 用于启动和停止服务
 */
@Slf4j
public class CommandUtil {
    /**
     * 执行外部进程并等待其完成
     *
     * @param processBuilder 外部进程的构建器对象
     * @return 如果执行成功返回true，否则返回false
     */
    public static boolean execute(ProcessBuilder processBuilder) {
        try {
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }
            return process.waitFor() == 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


}
