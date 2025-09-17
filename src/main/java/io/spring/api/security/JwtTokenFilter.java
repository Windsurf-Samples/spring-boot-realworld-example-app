package io.spring.api.security;

import io.spring.core.user.User;
import io.spring.infrastructure.service.AuthServiceClient;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class JwtTokenFilter extends OncePerRequestFilter {
  @Autowired private AuthServiceClient authServiceClient;
  private final String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    System.out.println("JwtTokenFilter: Processing request to " + request.getRequestURI());
    String authHeader = request.getHeader(header);
    System.out.println("JwtTokenFilter: Authorization header: " + authHeader);

    getTokenString(authHeader)
        .flatMap(
            token -> {
              System.out.println(
                  "JwtTokenFilter: Extracted token: "
                      + token.substring(0, Math.min(20, token.length()))
                      + "...");
              return authServiceClient.validateToken(token);
            })
        .ifPresent(
            userInfo -> {
              System.out.println(
                  "JwtTokenFilter: Token validation successful for user: "
                      + userInfo.getUsername());
              if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user =
                    new User(
                        userInfo.getEmail(),
                        userInfo.getUsername(),
                        "",
                        userInfo.getBio(),
                        userInfo.getImage());
                user.setId(userInfo.getId());
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
              }
            });

    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenString(String header) {
    if (header == null) {
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
