package org.alist.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
@Entity
@Table(name = "x_storages")
@NoArgsConstructor
public class Storage {

    @Id
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "请填写挂载路径")
    @Column(name = "mount_path")
    private String mountPath;

    @Column(name = "`order`") // 如果数据库中确实存在带反引号的字段名，则保留
    private Integer order;

    @NotEmpty(message = "请填写驱动")
    @Column(name = "driver")
    private String driver;

    @Column(name = "cache_expiration")
    private Integer cacheExpiration;

    @Column(name = "status")
    private String status;

    @Convert(converter = JsonConverter.class)
    @NotNull(message = "请填写附加信息")
    @Column(name = "addition")
    private Map<String, Object> addition;

    @Column(name = "remark")
    private String remark;

    @Column(name = "modified")
    private String modified;

    @Column(name = "disabled")
    private boolean disabled;

    @Column(name = "order_by")
    private String orderBy;

    @Column(name = "order_direction")
    private String orderDirection;

    @Column(name = "extract_folder")
    private String extractFolder;

    @Column(name = "web_proxy")
    private boolean webProxy;

    @Column(name = "webdav_policy")
    private String webdavPolicy;

    @Column(name = "down_proxy_url")
    private String downProxyUrl;

    public void build() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSSX");
        String formattedDate = formatter.format(ZonedDateTime.now());
        this.setOrder(0);
        this.setStatus("work");
        this.setExtractFolder("front");
        this.setOrderBy("name");
        this.setOrderDirection("asc");
        this.setCacheExpiration(30);
        this.setRemark("");
        this.setWebProxy(false);
        this.setDownProxyUrl("");
        this.setModified(formattedDate);
        this.setWebdavPolicy("302_redirect");
    }

}
