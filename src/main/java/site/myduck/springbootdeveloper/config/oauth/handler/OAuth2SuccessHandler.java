package site.myduck.springbootdeveloper.config.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.myduck.springbootdeveloper.config.jwt.TokenProvider;
import site.myduck.springbootdeveloper.config.oauth.CustomOAuth2User;
import site.myduck.springbootdeveloper.domain.Role;
import site.myduck.springbootdeveloper.repository.UserRepository;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        log.info("소셜 로그인 성공!");
        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            if(oAuth2User.getRole() == Role.GUEST) {
                String accessToken = tokenProvider.generateAccessToken(oAuth2User.getEmail());
                response.addHeader(tokenProvider.getAccessHeader(), "Bearer " + accessToken);
                response.sendRedirect("oauth2/sign-up");

                tokenProvider.sendAccessAndRefreshToken(response, accessToken, null);
            } else {
                loginSuccess(response, oAuth2User);
            }
        } catch (Exception e) {
            throw e;
        }

    }

    private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User)
            throws IOException {
        String accessToken = tokenProvider.generateAccessToken(oAuth2User.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken();
        response.addHeader(tokenProvider.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(tokenProvider.getRefreshHeader(), "Bearer " + refreshToken);

        tokenProvider.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        tokenProvider.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
    }
}
