package site.myduck.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import site.myduck.springbootdeveloper.domain.Article;
import site.myduck.springbootdeveloper.dto.AddArticleRequest;
import site.myduck.springbootdeveloper.dto.ArticleListViewResponse;
import site.myduck.springbootdeveloper.dto.UpdateArticleRequest;
import site.myduck.springbootdeveloper.repository.BlogRepository;

import java.util.List;

@RequiredArgsConstructor // final 키워드 혹은 @NotNull이 붙은 필드의 생성자 추가
@Service // 빈으로 등록
public class BlogService {

    private final BlogRepository blogRepository;

    // 글 추가 메서드
    public Article save(AddArticleRequest request, String userName) {
        return blogRepository.save(request.toEntity(userName));
    }
    
    // 조회수 증가 메서드
    public Article save(Article article) {
        return blogRepository.save(article);
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
        Article article = blogRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        authorizeArticleAuthor(article);
        blogRepository.delete(article);
    }

    // 글 수정 메서드
    @Transactional // 매칭한 메서드를 하나의 트랜잭션으로 묶는 역할
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    // 게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!article.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    // 페이징 처리
    public Page<ArticleListViewResponse> paging(Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 4; // 하나의 페이지에 보여줄 게시글의 개수

        // 한 페이지 당 5개의 게시글을 보여주고 ID를 기준으로 내림차순 정렬
        Page<Article> articlePages = blogRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));

        Page<ArticleListViewResponse> articleListViewResponses = articlePages.map(
                articlePage -> new ArticleListViewResponse(articlePage));

        return articleListViewResponses;
    }
}
