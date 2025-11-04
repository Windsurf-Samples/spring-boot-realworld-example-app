package io.spring.api.security;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.security.SecurityAuditLogger;
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
  @Autowired private UserRepository userRepository;
  @Autowired private JwtService jwtService;
  private final String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> tokenString = getTokenString(request.getHeader(header));

    if (tokenString.isEmpty()) {
      SecurityAuditLogger.logInvalidToken(request);
      filterChain.doFilter(request, response);
      return;
    }

    Optional<String> userId = jwtService.getSubFromToken(tokenString.get());
    if (userId.isEmpty()) {
      SecurityAuditLogger.logInvalidToken(request);
      filterChain.doFilter(request, response);
      return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      Optional<User> userOptional = userRepository.findById(userId.get());
      if (userOptional.isPresent()) {
        User user = userOptional.get();
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        SecurityAuditLogger.logAuthenticationSuccess(user.getUsername(), user.getId(), request);
      } else {
        SecurityAuditLogger.logInvalidToken(request);
      }
    }

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
