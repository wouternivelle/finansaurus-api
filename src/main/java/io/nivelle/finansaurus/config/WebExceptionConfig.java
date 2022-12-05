package io.nivelle.finansaurus.config;

import io.nivelle.finansaurus.balances.domain.InvalidBalanceException;
import io.nivelle.finansaurus.common.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class WebExceptionConfig extends ResponseEntityExceptionHandler {
    @ExceptionHandler(InvalidBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBalance(InvalidBalanceException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Invalid combination of " + exception.getMonth() + "/" + exception.getYear());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound() {
        return ResponseEntity.notFound().build();
    }
}
