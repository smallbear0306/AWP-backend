package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.LoginDTO;
import com.awp.dto.LoginVO;
import com.awp.dto.RegisterDTO;
import com.awp.entity.User;
import com.awp.mapper.UserMapper;
import com.awp.service.UserService;
import com.awp.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // 昵称缺省用用户名
        user.setNickname((dto.getNickname() == null || dto.getNickname().isBlank())
                ? dto.getUsername() : dto.getNickname());
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        String token = jwtUtil.generate(user.getId());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 不回传密码
        user.setPassword(null);
        return user;
    }
}
