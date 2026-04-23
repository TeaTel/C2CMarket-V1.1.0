package com.campus.backend.controller;

import com.campus.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diag")
public class DiagnosticsController {

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong - Controller is working!");
    }

    @GetMapping("/info")
    public Result<String> info() {
        return Result.success("Diagnostics endpoint - Spring MVC is active");
    }
}