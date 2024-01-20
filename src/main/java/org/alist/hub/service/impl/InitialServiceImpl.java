package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.api.Http;
import org.alist.hub.api.Payload;
import org.alist.hub.bean.Constants;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.InitialService;
import org.alist.hub.sql.SqlScriptBatchExecutor;
import org.alist.hub.utils.AListUtil;
import org.alist.hub.utils.ZipUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@AllArgsConstructor
public class InitialServiceImpl implements InitialService {
    private final SqlScriptBatchExecutor sqlExecutor;
    private final Http http;
    private final AppConfigRepository appConfigRepository;

    private void setting() throws IOException {
        // 获取 AList 的配置文件路径
        Path path = AListUtil.getConfig();
        // 读取配置文件内容
        String content = Files.readString(path);
        // 替换配置文件内容中的 IP 地址
        String result = content.replace("127.0.0.1", "0.0.0.0");
        // 替换配置文件内容中的令牌过期时间
        result = result.replace("ALIST_TOKEN_EXPIRE_TIME", "7200");
        // 重新写入配置文件
        Files.writeString(path, result);
    }


    public void createTable() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("db/migration/create.sql");
        byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
        String sql = new String(bytes, StandardCharsets.UTF_8);
        sqlExecutor.executeSQL(sql);
    }

    private void dropTable() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("db/migration/drop.sql");
        byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
        String sql = new String(bytes, StandardCharsets.UTF_8);
        sqlExecutor.executeSQL(sql);
    }


    @Override
    public boolean execute() {
        try {
            createTable();
            AppConfig appConfig = appConfigRepository.findByLabelAndGroup(Constants.APP_INIT, Constants.APP_GROUP);
            if (appConfig == null) {
                dropTable();
                setting();
                sqlExecutor.executeBatchedSQL(getUpdateSql(), 100);
                appConfig = new AppConfig();
                appConfig.setValue("true");
                appConfig.setLabel(Constants.APP_INIT);
                appConfig.setGroup(Constants.APP_GROUP);
                appConfig.setSafe(true);
                appConfigRepository.save(appConfig);
            }
            AListUtil.start();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public String getUpdateSql() throws Exception {
        String path = Constants.WORK_DIR + "/data/update.zip";
        http.downloadFile(Payload.create(Constants.XIAOYA_BASE_URL + "/update/update.zip"), path).block();
        ZipUtil.unzipFile(Path.of(path), Path.of(Constants.WORK_DIR + "/data"));
        return Constants.WORK_DIR + "/data/update.sql";
    }
}
