package com.korit.boardback.controller;

import com.korit.boardback.dto.request.ReqJoinDto;
import com.korit.boardback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody ReqJoinDto dto) {
        userService.join(dto);
        return ResponseEntity.ok().body(dto);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login() {
        return ResponseEntity.ok().build();
    }
}
