package io.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RealWorldApplication {

  public static void main(String[] args) {
    System.out.println(
        "DEBUG: Starting RealWorld Spring Boot Application with Java "
            + System.getProperty("java.version"));
    System.out.println("DEBUG: Application arguments: " + String.join(", ", args));
    SpringApplication.run(RealWorldApplication.class, args);
    System.out.println("DEBUG: RealWorld Application startup completed successfully");
  }
}
