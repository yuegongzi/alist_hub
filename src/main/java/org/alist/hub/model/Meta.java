package org.alist.hub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "x_meta")
public class Meta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String password;
    @Column(name = "p_sub")
    private boolean pSub;
    private boolean write;
    @Column(name = "w_sub")
    private boolean wSub;
    private String hide;
    @Column(name = "h_sub")
    private boolean hSub;
    private String readme;
    @Column(name = "r_sub")
    private boolean rSub;
}
