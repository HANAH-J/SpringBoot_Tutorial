package site.myduck.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.myduck.springbootdeveloper.domain.Article;
import site.myduck.springbootdeveloper.dto.ArticleListViewResponse;
import site.myduck.springbootdeveloper.dto.ArticleViewResponse;
import site.myduck.springbootdeveloper.service.BlogService;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BlogViewController {

    private final BlogService blogService;

    @GetMapping("/articles")
    public String getArticles(Model model,
                              @PageableDefault(page = 1)Pageable pageable) {
        List<ArticleListViewResponse> articles = blogService.findAll().stream()
                .map(ArticleListViewResponse::new)
                .toList();
        model.addAttribute("articles", articles); // 블로그 글 리스트 저장

        // 페이징 처리
        Page<ArticleListViewResponse> articlePages = blogService.paging(pageable);
        int blockLimit = 5;
        int startPage = (((int) Math.ceil(((double) pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), articlePages.getTotalPages());

        model.addAttribute("articlePages", articlePages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "articleList"; // articleList.html라는 뷰 조회
    }

    @GetMapping("/articles/{id}")
    public String getArticle(@PathVariable Long id, Model model) {
        Article article = blogService.findById(id);

        // 조회수 증가
        article.setRead_count(article.getRead_count() + 1);

        // 증가된 조회수로 업데이트된 Article 저장
        blogService.save(article);


        model.addAttribute("article", new ArticleViewResponse(article));

        return "article";
    }

    @GetMapping("/new-article")
    // id 키를 가진 쿼리 파라미터의 값을 id 변수에 매핑(id는 없을 수도 있다.)
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if (id == null) { // id가 없으면 생성
            model.addAttribute("article", new ArticleViewResponse());
        } else { // id가 없으면 수정
            Article article = blogService.findById(id);
            model.addAttribute("article", new ArticleViewResponse(article));
        }

        return "newArticle";
    }
}
