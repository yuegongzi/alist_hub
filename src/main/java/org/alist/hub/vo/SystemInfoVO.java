package org.alist.hub.vo;

import lombok.Data;
import org.alist.hub.bean.DiskUsageInfo;
import org.alist.hub.utils.DiskUsageUtil;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.Sensors;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class SystemInfoVO {
    private String osName;
    private Instant booted;
    private String uptime;
    private ComputerSystem system;
    private Long memoryTotal;
    private Long memoryAvailable;
    private Long swapTotal;
    private Long swapUsed;
    private double cpuLoaded;
    private Sensors sensors;
    private long diskTotal;
    private long diskUsed;
    private long bytesSend;
    private long bytesRecv;
    private String version;

    public static SystemInfoVO read() {
        SystemInfo si = new SystemInfo();
        SystemInfoVO vo = new SystemInfoVO();

        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        vo.setOsName(String.valueOf(os));
        vo.setBooted(Instant.ofEpochSecond(os.getSystemBootTime()));
        vo.setUptime(FormatUtil.formatElapsedSecs(os.getSystemUptime()));
        vo.setSystem(hal.getComputerSystem());
        vo.setMemoryTotal(hal.getMemory().getTotal());
        vo.setMemoryAvailable(hal.getMemory().getAvailable());
        vo.setSwapTotal(hal.getMemory().getVirtualMemory().getSwapTotal());
        vo.setSwapUsed(hal.getMemory().getVirtualMemory().getSwapUsed());
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        vo.setCpuLoaded(processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100);
        vo.setSensors(hal.getSensors());
        List<DiskUsageInfo> diskUsageList = DiskUsageUtil.getDiskUsage();
        long diskTotal = 0, diskUsed = 0L;
        for (DiskUsageInfo diskUsageInfo : diskUsageList) {
            diskTotal += diskUsageInfo.getTotal();
            diskUsed += diskUsageInfo.getUsed();
        }
        vo.setDiskUsed(diskUsed);
        vo.setDiskTotal(diskTotal);
        List<NetworkIF> networkIFs = hal.getNetworkIFs();
        AtomicLong bytesSend = new AtomicLong();
        AtomicLong bytesRecv = new AtomicLong();
        for (NetworkIF net : networkIFs) {
            bytesSend.addAndGet(net.getBytesSent());
            bytesRecv.addAndGet(net.getBytesRecv());
        }
        vo.setBytesRecv(bytesRecv.get());
        vo.setBytesSend(bytesSend.get());
        return vo;
    }
}
