package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_get_right_slug() {
    Article article = new Article("a new   title", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_get_right_slug_with_number_in_title() {
    Article article = new Article("a new title 2", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title-2"));
  }

  @Test
  public void should_get_lower_case_slug() {
    Article article = new Article("A NEW TITLE", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_handle_other_language() {
    Article article = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("中文-标题"));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }

  @Test
  public void should_create_article_with_all_fields() {
    DateTime createdAt = new DateTime();
    Article article =
        new Article(
            "Test Title",
            "description",
            "body content",
            Arrays.asList("java", "spring"),
            "user123",
            createdAt);

    assertThat(article.getId(), notNullValue());
    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getSlug(), is("test-title"));
    assertThat(article.getDescription(), is("description"));
    assertThat(article.getBody(), is("body content"));
    assertThat(article.getUserId(), is("user123"));
    assertThat(article.getCreatedAt(), is(createdAt));
    assertThat(article.getUpdatedAt(), is(createdAt));
    assertThat(article.getTags().size(), is(2));
  }

  @Test
  public void should_generate_unique_id_for_each_article() {
    Article article1 = new Article("Title 1", "desc", "body", Arrays.asList("java"), "user1");
    Article article2 = new Article("Title 2", "desc", "body", Arrays.asList("java"), "user2");

    assertThat(article1.getId(), not(article2.getId()));
  }

  @Test
  public void should_update_title_and_slug_when_not_empty() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "user123");
    DateTime originalUpdatedAt = article.getUpdatedAt();

    article.update("New Title", null, null);

    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getSlug(), is("new-title"));
    assertThat(
        article.getUpdatedAt().isAfter(originalUpdatedAt)
            || article.getUpdatedAt().equals(originalUpdatedAt),
        is(true));
  }

  @Test
  public void should_update_description_when_not_empty() {
    Article article = new Article("Title", "old desc", "body", Arrays.asList("java"), "user123");

    article.update(null, "new description", null);

    assertThat(article.getDescription(), is("new description"));
    assertThat(article.getTitle(), is("Title"));
  }

  @Test
  public void should_update_body_when_not_empty() {
    Article article = new Article("Title", "desc", "old body", Arrays.asList("java"), "user123");

    article.update(null, null, "new body content");

    assertThat(article.getBody(), is("new body content"));
    assertThat(article.getTitle(), is("Title"));
  }

  @Test
  public void should_not_update_fields_when_null() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), "user123");

    article.update(null, null, null);

    assertThat(article.getTitle(), is("Title"));
    assertThat(article.getDescription(), is("desc"));
    assertThat(article.getBody(), is("body"));
  }

  @Test
  public void should_not_update_fields_when_empty_string() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), "user123");

    article.update("", "", "");

    assertThat(article.getTitle(), is("Title"));
    assertThat(article.getDescription(), is("desc"));
    assertThat(article.getBody(), is("body"));
  }

  @Test
  public void should_update_multiple_fields_at_once() {
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), "user123");

    article.update("New Title", "new desc", "new body");

    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getSlug(), is("new-title"));
    assertThat(article.getDescription(), is("new desc"));
    assertThat(article.getBody(), is("new body"));
  }

  @Test
  public void should_deduplicate_tags() {
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java", "java", "spring"), "user123");

    assertThat(article.getTags().size(), is(2));
  }

  @Test
  public void should_have_equality_based_on_id() {
    Article article1 = new Article("Title", "desc", "body", Arrays.asList("java"), "user1");
    Article article2 = new Article("Title", "desc", "body", Arrays.asList("java"), "user1");

    assertThat(article1.equals(article2), is(false));
    assertThat(article1.equals(article1), is(true));
  }
}
