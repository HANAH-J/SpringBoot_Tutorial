package site.myduck.springbootdeveloper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import site.myduck.springbootdeveloper.config.jwt.TokenAuthenticationFilter;
import site.myduck.springbootdeveloper.config.jwt.TokenProvider;
import site.myduck.springbootdeveloper.config.login.CustomJsonAuthenticationFilter;
import site.myduck.springbootdeveloper.config.login.LoginFailureHandler;
import site.myduck.springbootdeveloper.config.login.LoginService;
import site.myduck.springbootdeveloper.config.login.LoginSuccessHandler;
import site.myduck.springbootdeveloper.config.oauth.CustomOAuth2UserService;
import site.myduck.springbootdeveloper.config.oauth.handler.OAuth2FailureHandler;
import site.myduck.springbootdeveloper.config.oauth.handler.OAuth2SuccessHandler;
import site.myduck.springbootdeveloper.repository.UserRepository;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final LoginService loginService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

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
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
                    .userInfoEndpoint().userService(customOAuth2UserService);

        // 토큰 방식으로 인증을 하기 때문에 세션 비활성화
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterAfter(customJsonAuthenticationFilter(), LogoutFilter.class);
        http.addFilterBefore(tokenAuthenticationFilter(), CustomJsonAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(tokenProvider, userRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public CustomJsonAuthenticationFilter customJsonAuthenticationFilter() {
        CustomJsonAuthenticationFilter customJsonUsernamePasswordLoginFilter
                = new CustomJsonAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customJsonUsernamePasswordLoginFilter;
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(tokenProvider, userRepository);
        return tokenAuthenticationFilter;
    }
}
