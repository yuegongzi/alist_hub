package org.alist.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "x_storages")
@NoArgsConstructor
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String mount_path;

    @Column(name = "`order`")
    private Integer order;

    private String driver;

    private Integer cache_expiration;

    private String status;

    private String addition;

    private String remark;

    @CreationTimestamp
    private LocalDateTime modified;

    private boolean disabled;

    private String order_by;

    private String order_direction;

    private String extract_folder;

    private boolean web_proxy;

    private String webdav_policy;

    private String down_proxy_url;
}
