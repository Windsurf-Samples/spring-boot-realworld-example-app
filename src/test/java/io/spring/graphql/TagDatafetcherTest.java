package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  private TagDatafetcher tagDatafetcher;

  @BeforeEach
  public void setUp() {
    tagDatafetcher = new TagDatafetcher(tagsQueryService);
  }

  @Test
  public void should_get_all_tags_success() {
    List<String> expectedTags = Arrays.asList("java", "spring", "graphql", "reactjs");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    List<String> result = tagDatafetcher.getTags();

    assertNotNull(result);
    assertEquals(4, result.size());
    assertTrue(result.contains("java"));
    assertTrue(result.contains("spring"));
    assertTrue(result.contains("graphql"));
    assertTrue(result.contains("reactjs"));
  }

  @Test
  public void should_return_empty_list_when_no_tags() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    List<String> result = tagDatafetcher.getTags();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void should_return_single_tag() {
    List<String> expectedTags = Arrays.asList("java");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    List<String> result = tagDatafetcher.getTags();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("java", result.get(0));
  }
}
