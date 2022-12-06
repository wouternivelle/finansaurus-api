package io.nivelle.finansaurus.authentication.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/login")
public class LoginController {
    @Value("${security.username}")
    private String username;
    @Value("${security.password}")
    private String password;

    @PostMapping
    public ResponseEntity<Map<String, String>> save(@RequestBody Map<String, String> resources) {
        if (password.equals(resources.get("password"))) {
            return ResponseEntity.ok(Map.of("user", username));
        }
        return ResponseEntity.notFound().build();
    }
}
