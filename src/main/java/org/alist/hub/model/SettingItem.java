package org.alist.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "x_setting_items")
public class SettingItem {
    @Id
    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

    @Column(name = "type")
    private String type;

    @Column(name = "options")
    private String options;

    @Column(name = "`group`") // 对于特殊字符或保留字如"group"，保持原样
    private Integer group;

    @Column(name = "flag")
    private Integer flag;

}
