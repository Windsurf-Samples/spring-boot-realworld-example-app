package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  public void should_create_with_defaults() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_create_with_custom_values() {
    Page page = new Page(5, 50);
    assertEquals(5, page.getOffset());
    assertEquals(50, page.getLimit());
  }

  @Test
  public void should_cap_limit_at_max() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void should_keep_default_limit_for_zero_or_negative() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());

    Page pageNeg = new Page(0, -5);
    assertEquals(20, pageNeg.getLimit());
  }

  @Test
  public void should_keep_default_offset_for_negative() {
    Page page = new Page(-1, 10);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void should_accept_positive_offset() {
    Page page = new Page(10, 10);
    assertEquals(10, page.getOffset());
  }
}
