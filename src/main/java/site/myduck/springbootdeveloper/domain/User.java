package site.myduck.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
public class User { // UserDetails를 상속받아 인증 객체로 사용

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Provider provider;  // 소셜 로그인 플랫폼 : google *일반 로그인의 경우 null

    private String providerId;  // 소셜 로그인 식별자 *일반 로그인의 경우 null

    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;

    // 사용자 권한 설정
    public void authorizeUser() {
        this.role = Role.USER;
    }

    // 비밀번호 암호화
    public void passwordEncode(BCryptPasswordEncoder encoder) {
        this.password = encoder.encode(this.password);
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }
}
