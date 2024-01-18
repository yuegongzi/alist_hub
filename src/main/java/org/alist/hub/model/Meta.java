package org.alist.hub.model;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Integer id;
    private  String password;
    private  boolean p_sub;
    private  boolean write;
    private  boolean w_sub;
    private  String hide;
    private  boolean h_sub;
    private  String  readme;
    private  boolean r_sub;
}
