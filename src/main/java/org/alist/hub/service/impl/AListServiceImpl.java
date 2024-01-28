package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.api.Http;
import org.alist.hub.api.Payload;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.XiaoYaBo;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.User;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.StorageService;
import org.alist.hub.utils.CommandUtil;
import org.alist.hub.utils.StringUtils;
import org.alist.hub.utils.ZipUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final Http http;

    @Override
    public boolean startNginx() {
        ClassPathResource classPathResource = new ClassPathResource("db/migration/nginx.conf");
        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String conf = new String(bytes, StandardCharsets.UTF_8);
            Files.writeString(Path.of("/etc/nginx/http.d/default.conf"), conf, StandardCharsets.UTF_8);
            stopNginx();
            return CommandUtil.execute(new ProcessBuilder("nginx"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean stopNginx() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("nginx", "-s", "stop");
        return CommandUtil.execute(builder);
    }

    /**
     * 启动 AList 服务。
     *
     * @return 如果成功启动返回 true，否则返回 false
     */
    @Override
    public boolean startAList() {
        try {
            ProcessBuilder alist = new ProcessBuilder();
            alist.command("/opt/alist/alist", "server", "--no-prefix");
            return CommandUtil.execute(alist);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void initialize(String password) {
        try {
            appConfigService.initialize();
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    update();
                    Optional<User> user = userRepository.findByUsername("admin");
                    user.map(u -> {
                        u.setDisabled(0);
                        u.setPassword(password);
                        return userRepository.save(u);
                    });
                    this.stopAList();
                    Thread.sleep(1000);
                    this.startAList();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }).start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
        return CommandUtil.execute(builder);
    }

    private void download(String file, String unzipPath) {
        try {
            // 指定下载文件路径
            String path = Constants.DATA_DIR + "/" + file;
            // 下载文件
            http.downloadFile(Payload.create(Constants.XIAOYA_BASE_URL + "/update/" + file), path).block();
            // 解压缩文件
            ZipUtil.unzipFile(Path.of(path), Path.of(unzipPath));
        } catch (Exception e) {
            // 打印异常信息
            log.error(e.getMessage(), e);
            // 抛出业务异常
            throw new ServiceException("下载文件失败");
        }
    }


    /**
     * 更新应用程序
     *
     * @return 更新是否成功
     */
    @Override
    public boolean update() {
        String version = checkUpdate();
        if (StringUtils.hasText(version) && appConfigService.isInitialized()) {
            download("update.zip", Constants.DATA_DIR);
            download("index.zip", "/index");
            download("tvbox.zip", "/www");
            storageService.removeAll();
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sqlite3", Constants.DATA_DIR + "/data.db", ".read " + Constants.DATA_DIR + "/update.sql");
            CommandUtil.execute(processBuilder);
            storageService.updateAliYunDrive();
            XiaoYaBo xiaoYaBo = new XiaoYaBo();
            xiaoYaBo.setUpdateTime(new Date());
            xiaoYaBo.setVersion(version);
            appConfigService.saveOrUpdate(xiaoYaBo);
        }
        return true;
    }

    /**
     * 检查是否有新版本更新
     *
     * @return 新版本号，若无更新返回null
     */
    private String checkUpdate() {
        // 获取最新版本号
        String version = http.get(Payload.create(Constants.XIAOYA_BASE_URL + "/version.txt")).getBody();
        version = version.replaceAll("[\n\r]+", "");

        // 查询已安装版本号
        Optional<XiaoYaBo> xiaoYaBoOptional = appConfigService.get(new XiaoYaBo(), XiaoYaBo.class);

        // 如果未安装则直接返回最新版本号
        if (xiaoYaBoOptional.isEmpty()) {
            return version;
        }

        // 如果最新版本号小于已安装版本号，则返回最新版本号
        if (StringUtils.compareVersions(xiaoYaBoOptional.get().getVersion(), version) < 0) {
            return version;
        }

        // 否则返回null，表示无更新
        return null;
    }

}
