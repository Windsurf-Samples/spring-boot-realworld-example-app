package io.spring.articles.core.user;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {
  private String id;
  private String email;
  private String username;
  private String password;
  private String bio;
  private String image;
  private DateTime createdAt;
  private DateTime updatedAt;

  public User(String email, String username, String password) {
    this.id = UUID.randomUUID().toString();
    this.email = email;
    this.username = username;
    this.password = password;
    this.createdAt = new DateTime();
    this.updatedAt = new DateTime();
  }
}
