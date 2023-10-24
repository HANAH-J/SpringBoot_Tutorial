package site.myduck.springbootdeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트 생성
@AutoConfigureMockMvc // MockMvc 생성
class TestControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach // 테스트 실행 전 실행하는 메서드
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach // 테스트 실행 후 실행하는 메서드
    public void cleanUp() {
        memberRepository.deleteAll();
    }

    @DisplayName("getAllmembers: 아티클 조회에 성공한다.")
    @Test
    public void gettAllMembers() throws Exception {
        // given ( 테스트 준비 ) : 멤버 저장
        final String url = "/test";
        Member savedMember = memberRepository.save(new Member(1L, "홍길동"));

        // when ( 테스트 진행 ) : 멤버 리스트를 조회하는 API 호출
        final ResultActions result = mockMvc.perform(get(url) // perform 메서드 : 요청을 전송하는 역할
                                                              //                 결과로 ResultActions 객체 회수
                                                              // ResultActions 객체 : 반환값을 검증 및 확인하는 andExpect() 메서드 제공
                .accept(MediaType.APPLICATION_JSON)); // accept() 메서드 : 요청 시 응답 타입 결정 ( ex. JSON, XML 등 )

        // then ( 테스트 결과 검증 ) : 응답 코드가 200 OK이고, 반환받은 값 중 0번째 요소의 id와 name이 저장된 값과 같은지 확인
        result
                .andExpect(status().isOk()) // andExpect() 메서드 : 응답 검증
                // 응답의 0번째 값이 DB에서 저장한 값과 같은지 확인
                .andExpect(jsonPath("$[0].id").value(savedMember.getId()))
                .andExpect(jsonPath("$[0].name").value(savedMember.getName()));
    }
}