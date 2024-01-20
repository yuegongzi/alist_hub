package org.alist.hub.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZipUtil {
    public static void unzipFile(Path zipFilePath, Path destDirectory) throws IOException, InterruptedException {
        // 检查目标目录是否存在，如果不存在则创建
        if (!Files.exists(destDirectory)) {
            Files.createDirectories(destDirectory);
        }
        // 构建解压命令
        ProcessBuilder pb = new ProcessBuilder("unzip", "-d", destDirectory.toString(), zipFilePath.toString());
        // 设置错误流和输出流重定向到当前进程的标准输出和标准错误（便于查看运行结果）
        pb.redirectErrorStream(true);
        // 启动子进程执行解压命令
        Process process = pb.start();
        // 等待进程执行完成，并读取其输出（可选，通常用于调试或者获取解压过程信息）
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to unzip file. Exit code: " + exitCode);
        }
    }
}
