package site.myduck.springbootdeveloper.dto;

import lombok.Builder;
import lombok.Getter;
import site.myduck.springbootdeveloper.config.oauth.userinfo.GoogleOAuth2UserInfo;
import site.myduck.springbootdeveloper.config.oauth.userinfo.OAuth2UserInfo;
import site.myduck.springbootdeveloper.domain.Provider;
import site.myduck.springbootdeveloper.domain.Role;
import site.myduck.springbootdeveloper.domain.User;

import java.util.Map;
import java.util.UUID;

@Getter
public class OAuthAttributes {
    private String nameAttributeKey;
    private OAuth2UserInfo oauth2UserInfo;

    @Builder
    private OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oauth2UserInfo) {
        this.nameAttributeKey = nameAttributeKey;
        this.oauth2UserInfo = oauth2UserInfo;
    }

    public static <SocialType> OAuthAttributes of(SocialType socialType,
                                                  String userNameAttributeName,
                                                  Map<String, Object> attributes) {
        return ofGoogle(userNameAttributeName, attributes);
    }

    public static OAuthAttributes ofGoogle(String userNameAttributeName,
                                           Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }

    public User toEntity(Provider provider, OAuth2UserInfo oauth2UserInfo) {
        return User.builder()
                .provider(provider)
                .providerId(oauth2UserInfo.getId())
                .email(UUID.randomUUID() + "@socialUser.com")
                .nickname(oauth2UserInfo.getNickname())
                .role(Role.GUEST)
                .build();
    }
}
