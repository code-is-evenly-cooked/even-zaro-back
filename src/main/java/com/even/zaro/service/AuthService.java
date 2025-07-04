package com.even.zaro.service;

import com.even.zaro.dto.auth.*;
import com.even.zaro.dto.jwt.JwtUserInfoDto;
import com.even.zaro.entity.*;
import com.even.zaro.global.ErrorCode;
import com.even.zaro.global.exception.user.UserException;
import com.even.zaro.global.jwt.JwtUtil;
import com.even.zaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    // 필드, 생성자
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final KakaoOAuthService kakaoOAuthService;
    private final EmailVerificationService emailVerificationService;
    private final RedisTemplate<String, String> redisTemplate;

    // 메서드
    // 회원가입
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto requestDto) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();
        String nickname = requestDto.getNickname();

        // 중복 확인
        Optional<User> pendingUser = userRepository.findByEmailAndNickname(email, nickname);

        if (pendingUser.isPresent() && pendingUser.get().getStatus() == Status.PENDING) {
            // 인증 메일 재전송
            emailVerificationService.resendVerificationEmail(pendingUser.get().getEmail());
            throw new UserException(ErrorCode.ALREADY_SIGNED_UP_NOT_VERIFIED_YET);
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserException(ErrorCode.EMAIL_ALREADY_EXISTED);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new UserException(ErrorCode.NICKNAME_ALREADY_EXISTED);
        }

        // 저장
        User user = userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .nickname(nickname)
                        .provider(Provider.LOCAL)
                        .status(Status.PENDING)
                        .build()
        );

        // 이메일 인증 메일 발송
        emailVerificationService.sendEmailVerification(user);

        return new SignUpResponseDto(user.getId(), user.getEmail(), user.getNickname());
    }

    // 로그인
    @Transactional
    public SignInResponseDto signIn(SignInRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UserException(ErrorCode.EMAIL_NOT_FOUND));

        validateNotDeleted(user);

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new UserException(ErrorCode.INCORRECT_PASSWORD);
        }

        return generateLoginResponse(user);
    }

    // 소셜 로그인 (카카오)
    @Transactional
    public SignInResponseDto SignInWithKakao(String kakaoAccessToken) {
        KakaoUserInfoDto kakaoUser = kakaoOAuthService.getUserInfo(kakaoAccessToken);
        Long kakaoId = kakaoUser.getId();
        String email = kakaoUser.getKakaoAccount().getEmail();
        String nickname = kakaoUser.getKakaoAccount().getProfile().getNickname();
        String profileImage = kakaoUser.getKakaoAccount().getProfile().getProfileImageUrl();
        Gender gender = Gender.fromKakao(kakaoUser.getKakaoAccount().getGender());

        // 이메일, 프로필 사진이 없는 경우 더미로 추가
        String safeEmail = (email != null) ? email : "kakao_" + kakaoId + "@kakao-user.com";

        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(Provider.KAKAO, kakaoId.toString());

        optionalUser.ifPresent(this::validateNotDeleted);

        User user = optionalUser
                .orElseGet(() -> userRepository.save(User.builder()
                                .provider(Provider.KAKAO)
                                .providerId(kakaoId.toString())
                                .email(safeEmail)
                                .nickname(nickname)
                                .profileImage(profileImage)
                                .gender(gender)
                                .status(Status.ACTIVE)
                                .build()));

        return generateLoginResponse(user);
    }

    // 로그인 연장 refresh
    @Transactional
    public RefreshResponseDto refreshAccessToken(String refreshToken) {
        String token = jwtUtil.extractBearerPrefix(refreshToken);

        if (!jwtUtil.validateRefreshToken(token)) {
            throw new UserException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtUtil.getUserIdFromRefreshToken(token);

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String savedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if (savedToken == null || !savedToken.equals(token)) {
            throw new UserException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.generateAccessToken(new JwtUserInfoDto(user.getId()));

        return new RefreshResponseDto(newAccessToken);
    }

    // 로그아웃
    @Transactional
    public void signOut(Long userId, String accessToken) {
        redisTemplate.delete("refresh:" + userId);

        Date expiration = jwtUtil.getAccessTokenExpiration(accessToken);
        long now = System.currentTimeMillis();
        long ttl = expiration.getTime() - now;

        redisTemplate.opsForValue()
                .set("BL:" + accessToken, "logout", ttl, TimeUnit.MILLISECONDS);
    }

    public SignInResponseDto generateLoginResponse(User user) {
        // 로그인 토큰 생성
        String[] tokens = jwtUtil.generateToken(new JwtUserInfoDto(user.getId()));
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        reactivateIfDormant(user);

        // lastLoginAt
        user.updateLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        try {
            redisTemplate.opsForValue()
                    .set("refresh:" + user.getId(), refreshToken, Duration.ofDays(7));
        } catch (Exception e) {
            log.error("Redis 토큰 저장 실패: {}", e.getMessage());
            // redis 저장 실패시 우선 로그만, 이후 로그인 실패 처리 등 확장 가능
        }

        return new SignInResponseDto(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getProvider()
        );
    }

    private void validateNotDeleted(User user) {
        if (user.getStatus() == Status.DELETED) {
            throw new UserException(ErrorCode.USER_ALREADY_DELETED);
        }
    }

    private void reactivateIfDormant(User user) {
        if (user.getStatus() == Status.DORMANT) {
            user.updateStatusToActive();
        }
    }
}
