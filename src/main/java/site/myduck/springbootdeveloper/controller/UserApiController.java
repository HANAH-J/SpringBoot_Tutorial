package site.myduck.springbootdeveloper.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.myduck.springbootdeveloper.dto.AddUserRequest;
import site.myduck.springbootdeveloper.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public String signUp(@RequestBody AddUserRequest dto) throws Exception {
        userService.signUp(dto);
        return "회원가입 성공!";
    }

    @PostMapping("/emailCheck")
    public Map<String, Boolean> emailCheck(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");

        boolean isDuplicate = userService.existEmail(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("duplicate", isDuplicate);

        return response;
    }

}
