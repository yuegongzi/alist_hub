package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.InitialService;
import org.alist.hub.util.CommandUtils;
import org.alist.hub.util.ZipUtils;
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
    private final AppConfigService appConfigService;
    private final AListService aListService;


    /**
     * 创建脚本
     *
     * @throws IOException 如果在读取类路径资源时发生I/O错误
     */
    private void createScript() throws IOException {
        // 创建类路径资源对象，指定资源路径为"db/migration/create.sh"
        ClassPathResource classPathResource = new ClassPathResource("db/migration/create.sh");

        // 将类路径资源的输入流拷贝到字节数组
        byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());

        // 将字节数组转换为字符串，指定编码为UTF-8
        String script = new String(bytes, StandardCharsets.UTF_8);

        // 将脚本内容写入路径为"/create.sh"的文件
        Files.writeString(Path.of("/create.sh"), script);

        // 执行命令"sh create.sh"
        CommandUtils.execute(new ProcessBuilder("sh", "/create.sh"));
    }


    @Override
    public boolean execute() {
        try {
            // 启动Nginx服务
            aListService.startNginx();
            // 创建脚本
            createScript();
            // 如果appConfigService已经初始化，则进行以下操作
            if (appConfigService.isInitialized()) {
                // 解压缩index.zip文件到/index目录
                ZipUtils.unzip(Constants.DATA_DIR + "/index.zip", "/index");
                // 解压缩tvbox.zip文件到/www目录
                ZipUtils.unzip(Constants.DATA_DIR + "/tvbox.zip", "/www");
                // 启动AList服务
                aListService.startAList();
            }
            return true;
        } catch (Exception e) {
            // 打印异常信息
            log.error(e.getMessage(), e);
        }
        return false;
    }


}
