package org.alist.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "x_app_config")
public class AppConfig {
    @Id
    private String id;
    private String value;
    @Column(name = "`group`")
    private Integer group;
}
