package site.myduck.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import site.myduck.springbootdeveloper.domain.Role;
import site.myduck.springbootdeveloper.domain.User;
import site.myduck.springbootdeveloper.dto.AddUserRequest;
import site.myduck.springbootdeveloper.repository.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public void signUp(AddUserRequest dto) throws Exception {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        userRepository.save(User.builder()
                                .email(dto.getEmail())
                                .password(encoder.encode(dto.getPassword()))
                                .role(Role.USER)
                                .build());
    }
}
