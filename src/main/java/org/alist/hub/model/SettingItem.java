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
    private String key;
    private String value;
    private String type;
    private String options;
    @Column(name = "`group`")
    private Integer group;
    private Integer flag;
}