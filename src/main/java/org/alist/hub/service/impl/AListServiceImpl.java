package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.utils.CommandUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * AListServiceImpl 是 AListService 的实现类。
 */
@Service
@Slf4j
@AllArgsConstructor
public class AListServiceImpl implements AListService {
    private final AppConfigRepository appConfigRepository;
    private AppConfigService appConfigService;

    /**
     * 设置文件内容
     */
    private void setFileContent() {
        List<AppConfig> list = appConfigRepository.findAllByGroup(Constants.FILE_GROUP);
        list.forEach(appConfig -> {
            Path path = Path.of(appConfig.getId());
            try {
                Files.writeString(path, appConfig.getValue(), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 设置配置。
     */
    private void setConfig() throws IOException {
        Path path = Path.of(Constants.WORK_DIR + "/data/config.json");
        String content = Files.readString(path, StandardCharsets.UTF_8);
        String result = content.replace("127.0.0.1", "0.0.0.0");
        result = result.replace("ALIST_TOKEN_EXPIRE_TIME", "7200");
        Files.writeString(path, result);
    }

    @Override
    public boolean startNginx() {
        ClassPathResource classPathResource = new ClassPathResource("db/migration/nginx.conf");
        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String conf = new String(bytes, StandardCharsets.UTF_8);
            Files.writeString(Path.of("/etc/nginx/http.d/default.conf"), conf, StandardCharsets.UTF_8);
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
            setFileContent();
            setConfig();
            ProcessBuilder alist = new ProcessBuilder();
            alist.command("/opt/alist/alist", "server", "--no-prefix");
            return CommandUtil.execute(alist);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void initialize() {
        try {
            setFileContent();
            stopNginx();
            ProcessBuilder entrypoint = new ProcessBuilder("/entrypoint.sh");
            CommandUtil.execute(entrypoint);//执行完成会启动nginx
            stopNginx();
            startNginx();//重新给nginx设置值
            appConfigService.initialize();
            this.startAList();
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
}
