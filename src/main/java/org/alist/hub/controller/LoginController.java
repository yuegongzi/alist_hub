package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.api.AList;
import org.alist.hub.dto.LoginDTO;
import org.alist.hub.vo.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class LoginController {
    private final AList aList;

    @PostMapping("/login")
    public LoginVO auth(@RequestBody @Valid LoginDTO login) {
        String token = aList.auth(login.getUsername(), login.getPassword());
        return new LoginVO("token");
    }


}
