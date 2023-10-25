package site.myduck.springbootdeveloper;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity // 엔티티로 지정
//@Entity(name = "member_list") // 'member_list' 이름을 가진 테이블과 매핑
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자
@AllArgsConstructor
public class Member {
    
    @Id // id 필드를 기본키로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 자동으로 1씩 증가
    //                                       .AUTO : 선택한 데이터베이스 방언 ( dialect ) 에 따라 방식을 자동으로 선택 ( 기본값 )
    //                                       .IDENTITY : 기본키 생성을 데이터베이스에 위암 ( AUTO_INCREMENT )
    //                                       .SEQUENCE : 데이터베이스 시퀀스를 사용해서 기본키를 할당 ( 오라클에서 주로 사용 )
    //                                       .TABLE : 키 생성 테이블 사용
    @Column(name = "id", updatable = false)
    private Long id;    // DB 테이블의 'id' 컬럼과 매칭
    
    @Column(name = "name", nullable = false) // name 이라는 not null 컬럼과 매핑
    //      unique : 컬럼의 유일한 값 ( unique ) 여부, 설정하지 않을 시 false
    //      columnDefinition : 컬럼 정보 설정
    private String name;    // DB 테이블의 'name' 컬럼과 매칭
}
