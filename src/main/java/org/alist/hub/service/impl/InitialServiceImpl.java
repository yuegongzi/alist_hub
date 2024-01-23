package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.api.Http;
import org.alist.hub.api.Payload;
import org.alist.hub.bean.Constants;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.InitialService;
import org.alist.hub.sql.SqlScriptBatchExecutor;
import org.alist.hub.utils.ZipUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Service
@Slf4j
@AllArgsConstructor
public class InitialServiceImpl implements InitialService {
    private final SqlScriptBatchExecutor sqlExecutor;
    private final Http http;
    private final AppConfigService appConfigService;
    private final AListService aListService;


    private void createTable() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("db/migration/create.sql");
        byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
        String sql = new String(bytes, StandardCharsets.UTF_8);
        sqlExecutor.executeSQL(sql);
    }

    private void createDir() {
        File file = new File("/data");
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public boolean execute() {
        try {
            createTable();
            createDir();
            aListService.startNginx();
            if (appConfigService.isInitialized()) {
//                aListService.startAList();//TODO 恢复
            }
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
