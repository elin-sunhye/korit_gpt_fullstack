package com.korit.boardback.service;

import com.korit.boardback.dto.request.ReqJoinDto;
import com.korit.boardback.dto.request.ReqLoginDto;
import com.korit.boardback.dto.response.RespTokenDto;
import com.korit.boardback.entity.User;
import com.korit.boardback.entity.UserRole;
import com.korit.boardback.exception.DuplicatedValueException;
import com.korit.boardback.exception.FieldError;
import com.korit.boardback.repository.UserRepository;
import com.korit.boardback.repository.UserRoleRepository;
import com.korit.boardback.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private FileService fileService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public boolean duplicatedByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public User join(ReqJoinDto reqDto) {
        if(duplicatedByUsername(reqDto.getUsername())) {
            throw new DuplicatedValueException(
                    List.of(
                            FieldError.builder()
                            .field("username")
                            .msg("이미 존재하는 사용자 이름 입니다.")
                            .build()
                    )
            );
        }

        User user = User.builder()
                .username(reqDto.getUsername())
                .password(passwordEncoder.encode(reqDto.getPassword()))
                .email(reqDto.getEmail())
                .nickname(reqDto.getUsername())
                .accountExpired(1)
                .accountLocked(1)
                .credentialsExpired(1)
                .accountEnabled(1)
                .build();

        userRepository.save(user);
        UserRole userRole = UserRole.builder()
                .userId(user.getUserId())
                .roleId(1)
                .build();
        userRoleRepository.save(userRole);
        return user;
    }

    public RespTokenDto login(ReqLoginDto dto) {
        String accessToken = null;
        String refreshToken = null;

//        받아온 dto username과 같은 user 정보가 있는지 확인 -> 없으면 에러 터짐
        User foundUser = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("시용자 정보를 확인하세요 :)"));

//        찾은 user 정보의 password랑 받아온 dto password 확인 -> 같지 않으면 에러 터짐
        if(!passwordEncoder.matches(dto.getPassword(), foundUser.getPassword())) {
            throw new BadCredentialsException("사용자 정보를 확인하세요.");
        }

//        user 정보도 있고 password도 같다면 jwtUtil로 accessToken refreshToken 생성
        accessToken = jwtUtil.generateToken(Integer.toString(foundUser.getUserId()), foundUser.getUsername(), true);
        refreshToken = jwtUtil.generateToken(Integer.toString(foundUser.getUserId()), foundUser.getUsername(), true);

        return RespTokenDto.builder()
                .type("JWT")
                .name("AccessToken")
                .token(accessToken)
                .build();
    }

    public void updateProfileImg(User user, MultipartFile file) {
        final String PROFILE_IMG_FILE_PATH = "/upload/user/profile";
        String saveFilename = fileService.saveFile(PROFILE_IMG_FILE_PATH, file); // 폴더에 저정
        userRepository.updateProfileImg(user.getUserId(), saveFilename); // 서버에 저장

//        이전 이미지가 있는지 없는지 확인
        if(user.getProfileImg() == null) {
            return;
        }
        fileService.delFile(PROFILE_IMG_FILE_PATH + "/" + user.getProfileImg()); // 폴더에 있는 이전 이미지 삭제
    }

    public void updateNickname(User user, String nickname) {
        userRepository.updateNickname(user.getUserId(), nickname);
    }
}
