package site.myduck.springbootdeveloper.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import site.myduck.springbootdeveloper.domain.Article;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class ArticleViewResponse {

    private Long id;
    private String title;
    private String content;
    private String author;
    private int read_count;
    private LocalDateTime createdAt;

    public ArticleViewResponse(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.author = article.getAuthor();
        this.read_count = article.getRead_count();
        this.createdAt = article.getCreatedAt();
    }
}
