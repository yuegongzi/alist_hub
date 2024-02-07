package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.dto.LoginDTO;
import org.alist.hub.external.AListClient;
import org.alist.hub.vo.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class LoginController {
    private final AListClient aListClient;

    @PostMapping("/login")
    public LoginVO login(@RequestBody @Valid LoginDTO login) {
        String token = aListClient.auth(login.getUsername(), login.getPassword());
        return new LoginVO(token);
    }


}
