package site.myduck.springbootdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.myduck.springbootdeveloper.domain.Article;

public interface BlogRepository extends JpaRepository<Article, Long> {
}
