package site.myduck.springbootdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.myduck.springbootdeveloper.domain.Provider;
import site.myduck.springbootdeveloper.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findByProvider(Provider provider, String providerId);
}
