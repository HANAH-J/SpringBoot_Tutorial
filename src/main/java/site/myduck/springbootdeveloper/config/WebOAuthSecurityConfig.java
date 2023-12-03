package site.myduck.springbootdeveloper.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import site.myduck.springbootdeveloper.config.jwt.TokenAuthenticationFilter;
import site.myduck.springbootdeveloper.config.jwt.TokenProvider;
import site.myduck.springbootdeveloper.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import site.myduck.springbootdeveloper.config.oauth.handler.OAuth2SuccessHandler;
import site.myduck.springbootdeveloper.repository.RefreshTokenRepository;
import site.myduck.springbootdeveloper.service.UserDetailService;
import site.myduck.springbootdeveloper.service.UserService;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final UserDetailService userDetailServiceOrigin;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests() // 인증, 인가 설정
                    .requestMatchers(toH2Console()).permitAll()
                    .requestMatchers("/img/**", "/css/**", "/js/**").permitAll()
                    .requestMatchers("/login", "/signup", "/user").permitAll()
                    .requestMatchers("/api/token").permitAll() // 토큰 재발급 URL은 인증 없이 접근 가능하도록 설정
                    .requestMatchers("/api/**").authenticated() // 나머지 API URL은 인증 필요
                    .anyRequest().permitAll()
                    .and()
                .formLogin() // 폼 기반 로그인 설정
                    .loginPage("/login")
                    .defaultSuccessUrl("/articles")
                    .and()
                .logout() // 로그아웃 설정
                    .logoutSuccessUrl("/login")
                    .invalidateHttpSession(true)
                    .and()
                .csrf().disable() // csrf 비활성화
                .httpBasic().disable()
                .oauth2Login()
                    .loginPage("/login")
                    .authorizationEndpoint()
                    // Authorization 요청과 관련된 상태 저장
                    .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                    .and()
                    .successHandler(oAuth2SuccessHandler()) // 인증 성공 시 실행할 핸들러
                    .userInfoEndpoint()
                    .userService(oAuth2UserCustomService);

        // 토큰 방식으로 인증을 하기 때문에 세션 비활성화
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 헤더를 확인할 커스텀 필터 추가
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // /api로 시작하는 url인 경우 401 상태 코드를 반환하도록 예외 처리
        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**"));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       BCryptPasswordEncoder bCryptPasswordEncoder,
                                                       UserDetailService userDetailService)
        throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailServiceOrigin) // 사용자 정보 서비스 설정
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService
        );
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }
}
