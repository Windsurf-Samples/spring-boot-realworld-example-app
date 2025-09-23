package io.spring.core.user;

public class User {
  private String id;
  private String email;
  private String username;
  private String password;
  private String bio;
  private String image;

  public User() {
    this.id = java.util.UUID.randomUUID().toString();
  }

  public User(String email, String username, String password, String bio, String image) {
    this.id = java.util.UUID.randomUUID().toString();
    this.email = email;
    this.username = username;
    this.password = password;
    this.bio = bio;
    this.image = image;
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getBio() {
    return bio;
  }

  public String getImage() {
    return image;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void update(String email, String username, String password, String bio, String image) {
    if (email != null && !email.isEmpty()) this.email = email;
    if (username != null && !username.isEmpty()) this.username = username;
    if (password != null && !password.isEmpty()) this.password = password;
    if (bio != null) this.bio = bio;
    if (image != null) this.image = image;
  }
}
