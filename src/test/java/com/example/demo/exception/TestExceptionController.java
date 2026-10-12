package com.example.demo.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestExceptionController {

    @GetMapping("/test/unexpected-error")
    String triggerUnexpectedError() {
        throw new RuntimeException("Test-only internal error");
    }
}