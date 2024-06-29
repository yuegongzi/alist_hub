package org.alist.hub.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.XiaoYaBo;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.SearchNodeService;
import org.alist.hub.service.StorageService;
import org.alist.hub.util.CommandUtils;
import org.alist.hub.util.StringUtils;
import org.alist.hub.util.ZipUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

/**
 * AListServiceImpl 是 AListService 的实现类。
 */
@Service
@Slf4j
@AllArgsConstructor
public class AListServiceImpl implements AListService {
    private final AppConfigService appConfigService;
    private final StorageService storageService;
    private final SearchNodeService searchNodeService;
    private final Http http;

    @Override
    public void startNginx() {
        ClassPathResource classPathResource = new ClassPathResource("db/migration/nginx.conf");
        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String conf = new String(bytes, StandardCharsets.UTF_8);
            Files.writeString(Path.of("/etc/nginx/http.d/default.conf"), conf, StandardCharsets.UTF_8);
            Path path = Paths.get("/run/nginx/nginx.pid");
            if (Files.exists(path)) {
                stopNginx();
            }
            CommandUtils.execute(new ProcessBuilder("nginx"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void stopNginx() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("nginx", "-s", "stop");
        CommandUtils.execute(builder);
    }

    /**
     * 启动 AList 服务。单开线程进行启动,避免一直阻塞影响其他服务
     *
     * @return 如果成功启动返回 true
     */
    @Override
    public boolean startAList() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                ProcessBuilder alist = new ProcessBuilder();
                alist.command("/opt/alist/alist", "server", "--no-prefix");
                CommandUtils.execute(alist);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).start();
        return true;
    }

    /**
     * 停止 AList 服务。
     *
     * @return 如果停止成功返回 true，否则返回 false
     */
    @Override
    public boolean stopAList() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("pkill", "-f", "/opt/alist/alist");
        return CommandUtils.execute(builder);
    }

    private void download(String file, String unzipPath) {
        try {
            // 指定下载文件路径
            String path = Constants.DATA_DIR + "/" + file;
            // 下载文件
            http.downloadFile(Payload.create(Constants.XIAOYA_BASE_URL + file), path).block();
            // 解压缩文件
            ZipUtils.unzipFile(Path.of(path), Path.of(unzipPath));
        } catch (Exception e) {
            // 打印异常信息
            log.error(e.getMessage(), e);
            // 抛出业务异常
            throw new ServiceException("下载文件失败");
        }
    }


    /**
     * 更新应用程序
     */
    @Override
    @Transactional(Transactional.TxType.NEVER)
    public void update() {
        if (appConfigService.isInitialized()) {
            this.stopAList();
            storageService.removeExpire();
            searchNodeService.update();
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sqlite3", Constants.DATA_DIR + "/data.db", ".read " + Constants.DATA_DIR + "/update.sql");
            CommandUtils.execute(processBuilder);
            storageService.resetStorage();//重新设置存储
            this.startAList();
        }
    }

    /**
     * 检查是否有新版本更新,且已下载完毕
     *
     * @return 新版本号，若无更新返回false
     */
    @Override
    public boolean checkUpdate() {
        // 获取最新版本号
        String version = http.get(Payload.create(Constants.XIAOYA_BASE_URL + "version.txt")).getBody();
        version = version.replaceAll("[\n\r]+", "");

        // 查询已安装版本号
        Optional<XiaoYaBo> xiaoYaBoOptional = appConfigService.get(new XiaoYaBo(), XiaoYaBo.class);

        // 如果未安装则直接返回最新版本号 或 最新版本号小于已安装版本号，则返回最新版本号
        if (xiaoYaBoOptional.isEmpty() || StringUtils.compareVersions(xiaoYaBoOptional.get().getVersion(), version) < 0) {
            try {
                download("update.zip", Constants.DATA_DIR);
                download("index.zip", "/index");
                download("tvbox.zip", "/www");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
            XiaoYaBo xiaoYaBo = new XiaoYaBo();
            xiaoYaBo.setUpdateTime(new Date());
            xiaoYaBo.setVersion(version);
            appConfigService.saveOrUpdate(xiaoYaBo);
            return true;
        }
        return false;
    }

}
