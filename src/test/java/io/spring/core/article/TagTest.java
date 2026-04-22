package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());
    assertNotNull(tag.getId());
  }

  @Test
  public void should_generate_unique_ids() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1.getId(), tag2.getId());
  }

  @Test
  public void should_have_equals_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1, tag2);
  }

  @Test
  public void should_not_be_equal_with_different_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1, tag2);
  }

  @Test
  public void should_have_same_hashcode_for_same_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }
}
