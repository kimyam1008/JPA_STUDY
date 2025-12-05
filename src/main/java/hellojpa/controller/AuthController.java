package hellojpa.controller;

import hellojpa.dto.LoginRequest;
import hellojpa.dto.SignupRequest;
import hellojpa.dto.TokenResponse;
import hellojpa.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원가입이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponse tokenResponse = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestParam String username) {
        authService.logout(username);
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃되었습니다.");
        return ResponseEntity.ok(response);
    }
}
