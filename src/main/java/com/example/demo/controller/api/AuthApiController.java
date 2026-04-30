package com.example.demo.controller.api;

import com.example.demo.config.JwtUtil;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.get("username"), body.get("password")));
            String token = jwtUtil.generateToken(auth.getName());
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            return ResponseEntity.ok(buildUserResponse(token, user, auth));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Tên đăng nhập hoặc mật khẩu không đúng"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(buildUserResponse(null, user, auth));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải có ít nhất 6 ký tự"));
        }
        // If current password provided, verify it (optional: skip for forced change)
        String currentPassword = body.get("currentPassword");
        if (currentPassword != null && !currentPassword.isBlank()) {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không đúng"));
            }
        } else if (!user.isMustChangePassword()) {
            // Require current password if not forced change
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng cung cấp mật khẩu hiện tại"));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    private Map<String, Object> buildUserResponse(String token, User user, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        if (token != null) response.put("token", token);
        response.put("username", user.getUsername());
        response.put("email", user.getEmail() != null ? user.getEmail() : "");
        response.put("cafeName", user.getCafe() != null ? user.getCafe().getName() : "");
        response.put("cafeId", user.getCafe() != null ? user.getCafe().getId() : 0L);
        response.put("mustChangePassword", user.isMustChangePassword());
        response.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        response.put("roles", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        // Include employeeId if linked
        if (user.getEmployee() != null) {
            response.put("employeeId", user.getEmployee().getId());
        }
        return response;
    }
}
