package site.myduck.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.myduck.springbootdeveloper.domain.Article;
import site.myduck.springbootdeveloper.dto.AddArticleRequest;
import site.myduck.springbootdeveloper.dto.UpdateArticleRequest;
import site.myduck.springbootdeveloper.repository.BlogRepository;

import java.util.List;

@RequiredArgsConstructor // final 키워드 혹은 @NotNull이 붙은 필드의 생성자 추가
@Service // 빈으로 등록
public class BlogService {

    private final BlogRepository blogRepository;

    // 글 추가 메서드
    public Article save(AddArticleRequest request) {
        return blogRepository.save(request.toEntity());
    }

    // 글 목록 조회 메서드
    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    // 글 상세 조회 메서드
    public Article findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    }

    // 글 삭제 메서드
    public void delete(long id) {
        blogRepository.deleteById(id);
    }

    // 글 수정 메서드
    @Transactional // 매칭한 메서드를 하나의 트랜잭션으로 묶는 역할
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        article.update(request.getTitle(), request.getContent());

        return article;
    }
}
