package hellojpa.service;

import hellojpa.dto.LoginRequest;
import hellojpa.dto.SignupRequest;
import hellojpa.dto.TokenResponse;
import hellojpa.entity.RefreshToken;
import hellojpa.entity.User;
import hellojpa.repository.RefreshTokenRepository;
import hellojpa.repository.UserRepository;
import hellojpa.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // 회원가입
    public void signup(SignupRequest request) {
        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    // 로그인
    public TokenResponse login(LoginRequest request) {
        // 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getUsername());

        // 리프레시 토큰 저장
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        saveRefreshToken(user, refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    public TokenResponse refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // DB에서 리프레시 토큰 조회
        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰을 찾을 수 없습니다."));

        // 만료 확인
        if (savedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(savedRefreshToken);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        // 새로운 액세스 토큰 생성
        User user = savedRefreshToken.getUser();
        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole().name());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    // 로그아웃
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        refreshTokenRepository.deleteByUser(user);
    }

    // 리프레시 토큰 저장
    private void saveRefreshToken(User user, String token) {
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7); // 7일 유효

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        refreshToken -> refreshToken.updateToken(token, expiredAt),
                        () -> {
                            RefreshToken newRefreshToken = RefreshToken.builder()
                                    .token(token)
                                    .user(user)
                                    .expiredAt(expiredAt)
                                    .build();
                            refreshTokenRepository.save(newRefreshToken);
                        }
                );
    }
}
