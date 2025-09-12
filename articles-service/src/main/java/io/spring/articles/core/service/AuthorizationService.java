package io.spring.articles.core.service;

import io.spring.articles.core.article.Article;
import io.spring.articles.core.user.User;

public class AuthorizationService {
  public static boolean canWriteArticle(User user, Article article) {
    return user.getId().equals(article.getUserId());
  }
}
