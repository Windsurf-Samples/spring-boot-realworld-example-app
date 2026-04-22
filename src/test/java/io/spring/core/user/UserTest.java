package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@email.com", "testuser", "password", "bio", "image");
    assertEquals("test@email.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
    assertNotNull(user.getId());
  }

  @Test
  public void should_create_user_with_unique_id() {
    User user1 = new User("a@email.com", "user1", "pass", "", "");
    User user2 = new User("b@email.com", "user2", "pass", "", "");
    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  public void should_update_email() {
    User user = new User("old@email.com", "user", "pass", "bio", "img");
    user.update("new@email.com", "", "", "", "");
    assertEquals("new@email.com", user.getEmail());
    assertEquals("user", user.getUsername());
  }

  @Test
  public void should_update_username() {
    User user = new User("test@email.com", "oldname", "pass", "bio", "img");
    user.update("", "newname", "", "", "");
    assertEquals("newname", user.getUsername());
    assertEquals("test@email.com", user.getEmail());
  }

  @Test
  public void should_update_password() {
    User user = new User("test@email.com", "user", "oldpass", "bio", "img");
    user.update("", "", "newpass", "", "");
    assertEquals("newpass", user.getPassword());
  }

  @Test
  public void should_update_bio_and_image() {
    User user = new User("test@email.com", "user", "pass", "oldbio", "oldimg");
    user.update("", "", "", "newbio", "newimg");
    assertEquals("newbio", user.getBio());
    assertEquals("newimg", user.getImage());
  }

  @Test
  public void should_not_update_fields_with_empty_values() {
    User user = new User("test@email.com", "user", "pass", "bio", "img");
    user.update("", "", "", "", "");
    assertEquals("test@email.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  public void should_not_update_fields_with_null_values() {
    User user = new User("test@email.com", "user", "pass", "bio", "img");
    user.update(null, null, null, null, null);
    assertEquals("test@email.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  public void should_update_all_fields_at_once() {
    User user = new User("old@email.com", "olduser", "oldpass", "oldbio", "oldimg");
    user.update("new@email.com", "newuser", "newpass", "newbio", "newimg");
    assertEquals("new@email.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimg", user.getImage());
  }

  @Test
  public void should_have_equals_based_on_id() {
    User user1 = new User("a@email.com", "user1", "pass", "", "");
    User user2 = new User("b@email.com", "user2", "pass", "", "");
    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }
}
