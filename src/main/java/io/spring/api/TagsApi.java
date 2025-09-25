package io.spring.api;

import io.spring.application.TagsQueryService;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  @GetMapping
  public ResponseEntity getTags() {
    System.out.println("DEBUG: GET /tags - Fetching all tags");
    var tags = tagsQueryService.allTags();
    System.out.println("DEBUG: GET /tags - Found " + tags.size() + " tags");
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tags);
          }
        });
  }
}
