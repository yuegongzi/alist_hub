package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.model.AppConfig;
import org.alist.hub.model.User;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.utils.CommandUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

/**
 * AListServiceImpl 是 AListService 的实现类。
 */
@Service
@Slf4j
@AllArgsConstructor
public class AListServiceImpl implements AListService {
    private final AppConfigRepository appConfigRepository;
    private final AppConfigService appConfigService;
    private final UserRepository userRepository;

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
            setFileContent();
            stopNginx();
            ProcessBuilder entrypoint = new ProcessBuilder("/entrypoint.sh");
            CommandUtil.execute(entrypoint);//执行完成会启动nginx
            stopNginx();
            startNginx();// 重新给nginx设置值
            appConfigService.initialize();
            if (StringUtils.hasText(password)) {
                // 更新admin用户密码
                Optional<User> user = userRepository.findByUsername("admin");
                user.map(u -> {
                    u.setDisabled(0);
                    u.setPassword(password);
                    return userRepository.save(u);
                });

            }
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
