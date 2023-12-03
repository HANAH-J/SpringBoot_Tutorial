package site.myduck.springbootdeveloper.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import site.myduck.springbootdeveloper.config.jwt.TokenProvider;
import site.myduck.springbootdeveloper.config.oauth.PasswordUtil;
import site.myduck.springbootdeveloper.domain.User;
import site.myduck.springbootdeveloper.repository.UserRepository;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String NO_CHECK_URL = "/login";    // "/login"으로 들어오는 요청은 Filter 작동 X
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);        // "/login"으로 들어오는 요청은 다음의 필터 호출
            return;
        }

        // 사용자 요청 Header에서 RefreshToken 추출
        // DB에 RefreshToken이 없거나 유효하지 않을 경우 null 반환
        // DB에 RefreshToken이 있을 경우 AccessToken이 만료되어 요청한 것
        String refreshToken = tokenProvider.extractRefreshToken(request)
                .filter(tokenProvider::isTokenValid)
                .orElse(null);

        // 사용자 요청 Header에 RefreshToekn 존재하는 경우 AceessToken이 만료되어 요청한 것이므로
        // DB의 RefreshToken과 일치 여부 확인 후 AccessToken 재발급
        if (refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return;
        }

        // DB에 RefreshToken이 없거나 유효하지 않을 경우 AccessToken을 검사하고 인증을 처리
        // AccessToken이 없거나 유효하지 않다면 인증 객체가 담기지 않은 상태로 다음 필터로 넘어가므로 403 에러 발생
        // AccessToken이 유효하다면 인증 객체가 담긴 상태로 다음 필터로 넘어가기
        if (refreshToken == null) {
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }
    }

    // RefreshToken으로 사용자 정보 검색 & AccessToken/RefreshToken 재발급
    // 파라미터로 들어온 Header에서 추출한 RefreshToken으로 DB에서 사용자를 찾고
    // 해당하는 사용자가 있는 경우 TokenProvider.generateAccessToken() 실행하여 AccessToken 생성
    // reIssueRefreshToken() 실행하여 RefreshToken 재발급 & DB에 RefreshToken 업데이트
    // 이후 TokenProvider.sendAccessTokenAndRefreshToken() 실행하여 응답 Header에 전송
    public void checkRefreshTokenAndReIssueAccessToken(
            HttpServletResponse response,
            String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    tokenProvider.sendAccessAndRefreshToken(response, tokenProvider.generateAccessToken(user.getEmail()),
                            reIssuedRefreshToken);
                });
    }

    // RefreshToken 재발급 & DB에 RefreshToken 업데이트
    // TokenProvider.generateRefreshToken() 실행하여 RefreshToken 재발급 후
    // DB에 재발급한 RefreshToken 업데이트하여 Flush
    private String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = tokenProvider.generateRefreshToken();
        user.updateRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    // AccessToken 확인 & 인증 처리
    // request에서 extractAccessToken() 실행하여 AccessToken 추출
    // isTokenValid() 실행하여 토큰 유효성 검증
    // 유효한 토큰일 경우, AccessToken에서 extractEmail로 email 추출, findByEmail()로 해당 사용자 검색
    // 사용자 객체를 saveAuthentication() 실행하여 인증 처리
    // 인증 허가된 객체를 SecurityContextHolder에 저장하여 다음 인증 필터로 진행
    public void checkAccessTokenAndAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");
        tokenProvider.extractAccessToken(request)
                .filter(tokenProvider::isTokenValid)
                .ifPresent(accessToken -> tokenProvider.extractEmail(accessToken)
                        .ifPresent(email -> userRepository.findByEmail(email)
                                .ifPresent(this::saveAuthentication)));

        filterChain.doFilter(request, response);
    }

    // 인증 허가
    public void saveAuthentication(User myUser) {
        String password = myUser.getPassword();
        if (password == null) { // 소셜 로그인 사용자의 비밀번호를 임의로 설정하여 소셜 로그인 사용자도 인증 되도록 설정
            password = PasswordUtil.generateRandomPassword();
        }

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(myUser.getEmail())
                .password(password)
                .roles(myUser.getRole().name())
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}


