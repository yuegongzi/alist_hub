package org.alist.hub.util;

import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.DiskUsageInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DiskUsageUtils {

    public static List<DiskUsageInfo> getDiskUsage() {
        List<DiskUsageInfo> diskUsageList = new ArrayList<>();

        try {
            // 执行 Linux 命令：df /
            Process process = new ProcessBuilder("df", "/").start();

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // 跳过命令输出的标题行
            reader.readLine();

            while ((line = reader.readLine()) != null) {

                // 解析每行内容并创建 DiskUsageInfo 对象
                DiskUsageInfo diskUsageInfo = parseDiskUsageInfo(line);
                if (diskUsageInfo != null) {
                    diskUsageList.add(diskUsageInfo);
                }
            }

            // 等待命令执行完成
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        return diskUsageList;
    }

    private static DiskUsageInfo parseDiskUsageInfo(String line) {
        // 解析每行内容并创建 DiskUsageInfo 对象
        String[] parts = line.split("\\s+");
        if (parts.length > 4) {
            DiskUsageInfo diskUsageInfo = new DiskUsageInfo();
            diskUsageInfo.setFileSystem(parts[0]);
            diskUsageInfo.setTotal(Long.parseLong(parts[1]));
            diskUsageInfo.setUsed(Long.parseLong(parts[2]));
            diskUsageInfo.setAvailable(Long.parseLong(parts[3]));
            diskUsageInfo.setUsagePercentage(Double.parseDouble(parts[4].replace("%", "")));
            return diskUsageInfo;
        }
        return null;
    }
}
