package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.spring.infrastructure.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class MyBatisConfigurationTest {

  @Autowired private UserMapper userMapper;

  @Test
  public void testUserMapperIsLoaded() {
    assertNotNull(userMapper, "UserMapper should be loaded by Spring");
  }
}
