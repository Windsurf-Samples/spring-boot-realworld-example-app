package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.spring.TestHelper.articleDataFixture;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.ArticleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticlesApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ListArticleApiTest extends TestWithCurrentUser {
  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @Autowired private MockMvc mvc;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_default_article_list() throws Exception {
    ArticleDataList articleDataList =
        new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(0, 20)), eq(null)))
        .thenReturn(articleDataList);
    RestAssuredMockMvc.when().get("/articles").prettyPeek().then().statusCode(200);
  }

  @Test
  public void should_get_feeds_401_without_login() throws Exception {
    RestAssuredMockMvc.when().get("/articles/feed").prettyPeek().then().statusCode(401);
  }

  @Test
  public void should_get_feeds_success() throws Exception {
    ArticleDataList articleDataList =
        new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
    when(articleQueryService.findUserFeed(eq(user), eq(new Page(0, 20))))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/feed")
        .prettyPeek()
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_articles_with_custom_limit() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(0, 10)), eq(null)))
        .thenReturn(articleDataList);

    given().queryParam("limit", 10).when().get("/articles").then().statusCode(200);
  }

  @Test
  public void should_get_articles_with_custom_offset() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(5, 20)), eq(null)))
        .thenReturn(articleDataList);

    given().queryParam("offset", 5).when().get("/articles").then().statusCode(200);
  }

  @Test
  public void should_get_articles_with_custom_offset_and_limit() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(10, 5)), eq(null)))
        .thenReturn(articleDataList);

    given()
        .queryParam("offset", 10)
        .queryParam("limit", 5)
        .when()
        .get("/articles")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_empty_article_list() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(), 0);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(0, 20)), eq(null)))
        .thenReturn(articleDataList);

    RestAssuredMockMvc.when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_articles_filtered_by_tag() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findRecentArticles(
            eq("java"), eq(null), eq(null), eq(new Page(0, 20)), eq(null)))
        .thenReturn(articleDataList);

    given().queryParam("tag", "java").when().get("/articles").then().statusCode(200);
  }

  @Test
  public void should_get_articles_filtered_by_author() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findRecentArticles(
            eq(null), eq("johnjacob"), eq(null), eq(new Page(0, 20)), eq(null)))
        .thenReturn(articleDataList);

    given().queryParam("author", "johnjacob").when().get("/articles").then().statusCode(200);
  }

  @Test
  public void should_get_articles_filtered_by_favorited() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq("johnjacob"), eq(new Page(0, 20)), eq(null)))
        .thenReturn(articleDataList);

    given().queryParam("favorited", "johnjacob").when().get("/articles").then().statusCode(200);
  }

  @Test
  public void should_get_articles_with_authentication() throws Exception {
    ArticleDataList articleDataList =
        new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(0, 20)), eq(user)))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_401_with_invalid_token_when_get_feed() throws Exception {
    String invalidToken = "invalid-token";
    when(jwtService.getSubFromToken(eq(invalidToken))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + invalidToken)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_feeds_with_custom_limit() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findUserFeed(eq(user), eq(new Page(0, 10))))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .queryParam("limit", 10)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_feeds_with_custom_offset() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findUserFeed(eq(user), eq(new Page(5, 20))))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .queryParam("offset", 5)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_feeds_with_custom_offset_and_limit() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findUserFeed(eq(user), eq(new Page(10, 5))))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .queryParam("offset", 10)
        .queryParam("limit", 5)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_empty_feed_list() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(), 0);
    when(articleQueryService.findUserFeed(eq(user), eq(new Page(0, 20))))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_articles_with_large_offset_returns_empty() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(), 0);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(1000, 20)), eq(null)))
        .thenReturn(articleDataList);

    given()
        .queryParam("offset", 1000)
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_articles_with_multiple_filters() throws Exception {
    ArticleDataList articleDataList = new ArticleDataList(asList(articleDataFixture("1", user)), 1);
    when(articleQueryService.findRecentArticles(
            eq("java"), eq("johnjacob"), eq(null), eq(new Page(0, 20)), eq(null)))
        .thenReturn(articleDataList);

    given()
        .queryParam("tag", "java")
        .queryParam("author", "johnjacob")
        .when()
        .get("/articles")
        .then()
        .statusCode(200);
  }
}
