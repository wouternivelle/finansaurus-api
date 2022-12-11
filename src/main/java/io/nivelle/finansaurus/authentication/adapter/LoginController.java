package io.nivelle.finansaurus.authentication.adapter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/login")
public class LoginController {
    @RequestMapping(method = {RequestMethod.HEAD})
    public ResponseEntity<Void> login() {
        return ResponseEntity.noContent().build();
    }
}
