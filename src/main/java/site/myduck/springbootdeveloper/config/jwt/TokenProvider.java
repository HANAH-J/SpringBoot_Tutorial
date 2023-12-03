package site.myduck.springbootdeveloper.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.myduck.springbootdeveloper.domain.User;
import site.myduck.springbootdeveloper.repository.UserRepository;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Slf4j
@Service
public class TokenProvider {

    // application.yml 프로퍼티 주입
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    // JWT Header 값 : 'Authorization(Key) = Bearer {토큰} (Value)'
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer";

    private final UserRepository userRepository;

    
    
    // AccessToken & RefreshToken 생성
    // AccessToken 생성
    public String generateAccessToken(String email) {
        Date now = new Date();

        return JWT.create()                             // JWT 토큰 생성 빌더 반환
                // JWT Payload Claim 생성
                .withSubject(ACCESS_TOKEN_SUBJECT)      // JWT Subject 지정
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod)) // 토큰 만료 시간 설정
                // 사용자 정의 Claim 생성
                .withClaim(EMAIL_CLAIM, email)          // email Claim 사용
                // 사용할 알고리즘과 서버 개인 키 지정 -> JWT 토큰이 암호화되어 생성
                .sign(Algorithm.HMAC512(secretKey));    // HMAC512 알고리즘 사용
    }

    // RefreshToken 생성
    public String generateRefreshToken() {
        Date now = new Date();

        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));    // email Claim 필요 X
    }



    // AccessToken & RefreshToken Response Header 추가
    // AccessToken Header 전송
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        // 주입받은 accessHeader를 키로 사용하여 생성한 accessToken 전송
        response.setHeader(accessHeader, accessToken);
        log.info("재발급 AccessToken: ", accessToken);
    }

    // AccessToken + RefreshToken Header 전송
    public void sendAccessAndRefreshToken(HttpServletResponse response,
                                          String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        // 주입받은 accessHeader를 키로 사용하여 accessToken, refreshToken 전송
        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("AccessToken 및 RefreshToken 헤더 설정 완료");
    }

    
    
    // 클라이언트 요청에 의해 JWT, Email 추출
    // AccessToken Header 추출
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                // 토큰 형식에서 'Bearer' 제외하여 replace
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    // RefreshToken Header 추출
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                // 토큰 형식에서 'Bearer' 제외하여 replace
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    // AccessToken email 추출
    public Optional<String> extractEmail(String accessToken) {
        try {
            // 추출 전 JWT.require()로 검증기 생성
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)    // accessToken 검증
                    .getClaim(EMAIL_CLAIM)  // 유효한 경우 email Claim 추출
                    .asString());
        } catch (Exception e) {
            log.error("액세스 토큰이 유효하지 않습니다.");
            return Optional.empty();        // 유효하지 않은 경우 Optional 객체 반환
        }
    }

    
    
    // AccessToken & RefreshToken Header 설정
    // AccessToken Header 설정
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }
    
    // RefreshToken Header 설정
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }



    // RefreshToken DB 저장(업데이트)
    public void updateRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> new Exception("일치하는 회원이 없습니다.")
                );
    }

    
    
    // JWT 유효성 검사
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다.", e.getMessage());
            return false;
        }
    }
}
