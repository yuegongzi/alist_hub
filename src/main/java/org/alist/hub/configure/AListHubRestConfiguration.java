package org.alist.hub.configure;

import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
/**
 * AListHubRestConfiguration类是<a href="https://docs.spring.io/spring-data/rest/docs/current-SNAPSHOT/reference/html/#getting-started.maven">Spring Data REST</a>配置类。
 */
@Component
public class AListHubRestConfiguration implements RepositoryRestConfigurer {


    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // 设置基本路径为"/api/v1"
        config.setBasePath("/api/v1");
        // 设置默认每页显示数量为10
        config.setDefaultPageSize(10);
        // 设置最大每页显示数量为100
        config.setMaxPageSize(100);
        // 在创建资源时返回主体
        config.setReturnBodyOnCreate(true);
        // 在更新资源时返回主体
        config.setReturnBodyOnUpdate(true);
        cors.addMapping("/**")
                .allowedOrigins("*") // 允许所有域访问
                .allowedHeaders("Content-Type", "Authorization") // 限制允许的头部信息
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE") // 限制允许的方法
                .allowCredentials(false) // 不允许携带凭据
                .maxAge(3600); // 设置缓存超时时间为3600秒
        // 调用RepositoryRestConfigurer的父类方法来配置仓库的REST配置
        RepositoryRestConfigurer.super.configureRepositoryRestConfiguration(config, cors);
    }

}
