package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.TagsQueryService;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TagsApiTest {

  @Autowired private MockMvc mvc;

  @MockBean private TagsQueryService tagsQueryService;

  @MockBean private JwtService jwtService;

  @MockBean private UserRepository userRepository;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_all_tags_success() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "reactjs"));

    given()
        .contentType("application/json")
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags", hasItems("java", "spring", "reactjs"));
  }

  @Test
  public void should_return_empty_tags_when_no_tags_exist() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList());

    given().contentType("application/json").when().get("/tags").then().statusCode(200);
  }
}
