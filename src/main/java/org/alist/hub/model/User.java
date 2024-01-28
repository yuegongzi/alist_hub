package org.alist.hub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "x_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "base_path")
    private String basePath;

    @Column(name = "role")
    private Integer role;

    @Column(name = "disabled")
    private Integer disabled;

    @Column(name = "permission")
    private Integer permission;

    @Column(name = "otp_secret")
    private String otpSecret;

    @Column(name = "sso_id")
    private String ssoId;

}
