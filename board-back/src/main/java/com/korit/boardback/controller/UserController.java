package com.korit.boardback.controller;

import com.korit.boardback.entity.User;
import com.korit.boardback.security.principal.PrincipalUser;
import com.korit.boardback.service.EmailService;
import com.korit.boardback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @Operation(summary = "로그인한 유저 정보")
    @GetMapping("/user/me")
//    두개 같은거임 가져오는 방법은 2가지
//    @AuthenticationPrincipal PrincipalUser principalUser
//    PrincipalUser principalUser2 = (PrincipalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    public ResponseEntity<User> getLoginUser(@AuthenticationPrincipal PrincipalUser principalUser) {
        if(principalUser.getUser().getProfileImg() == null) {
            principalUser.getUser().setProfileImg("default.png");
        }
        return ResponseEntity.ok().body(principalUser.getUser());
    }

    @Operation(summary = "프로필 이미지 변경")
    @PostMapping("/user/profile/img")
    public ResponseEntity<?> changeProfileImg(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @RequestPart MultipartFile file
    ) {
        userService.updateProfileImg(principalUser.getUser(), file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "닉네임 변경")
    @PutMapping("/user/profile/nickname")
    public ResponseEntity<?> changeNickname(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @RequestBody Map<String, String> reqBody
    ) {
        String nickname = reqBody.get("nickname");
        userService.updateNickname(principalUser.getUser(), nickname);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/user/profile/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @RequestBody Map<String, String> reqBody
    ) {
        String password = reqBody.get("password");
        userService.updatePassword(principalUser.getUser(), password);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/profile/email/verification")
    public ResponseEntity<String> sendChangeEmailVerification(
            @RequestBody Map<String, String> reqBody
    ) throws MessagingException {
        String email = reqBody.get("email");
        String code = emailService.generateEmailCode();
        emailService.sendChangeEmailVerification(email, code);
        return ResponseEntity.ok().body(code);
    }

    @PutMapping("/user/profile/email")
    public ResponseEntity<String> changeEmail(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @RequestBody Map<String, String> reqBody
    ) {
        String email = reqBody.get("email");
        userService.updateEmail(principalUser.getUser(), email);
        return ResponseEntity.ok().build();
    }
}
