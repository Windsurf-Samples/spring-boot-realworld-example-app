package io.spring.infrastructure.security;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class SecurityAuditLogger {
  private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

  public static void logAuthenticationSuccess(
      String username, String userId, HttpServletRequest request) {
    setMDC(username, userId, request, "AUTHENTICATION", "SUCCESS");
    securityLogger.info("User authentication successful");
    MDC.clear();
  }

  public static void logAuthenticationFailure(String attemptedEmail, HttpServletRequest request) {
    setMDC(attemptedEmail, null, request, "AUTHENTICATION", "FAILURE");
    securityLogger.warn("User authentication failed");
    MDC.clear();
  }

  public static void logAccountCreation(
      String username, String userId, HttpServletRequest request) {
    setMDC(username, userId, request, "ACCOUNT_CREATE", "SUCCESS");
    securityLogger.info("User account created");
    MDC.clear();
  }

  public static void logAccountModification(
      String username, String userId, HttpServletRequest request) {
    setMDC(username, userId, request, "ACCOUNT_MODIFY", "SUCCESS");
    securityLogger.info("User account modified");
    MDC.clear();
  }

  public static void logAuthorizationFailure(
      String username, String userId, String resource, HttpServletRequest request) {
    setMDC(username, userId, request, "AUTHORIZATION", "DENIED");
    MDC.put("resource", resource);
    securityLogger.warn("Authorization denied for resource access");
    MDC.clear();
  }

  public static void logInvalidToken(HttpServletRequest request) {
    setMDC(null, null, request, "TOKEN_VALIDATION", "FAILURE");
    securityLogger.warn("Invalid or missing JWT token");
    MDC.clear();
  }

  public static void logSecurityException(
      String exceptionType, String message, HttpServletRequest request) {
    setMDC(null, null, request, "SECURITY_EXCEPTION", "ERROR");
    MDC.put("exceptionType", exceptionType);
    securityLogger.error("Security exception occurred: " + message);
    MDC.clear();
  }

  private static void setMDC(
      String username, String userId, HttpServletRequest request, String eventType, String result) {
    MDC.put("correlationId", generateCorrelationId());
    MDC.put("username", username != null ? username : "anonymous");
    MDC.put("userId", userId != null ? userId : "N/A");
    MDC.put("sourceIp", getClientIp(request));
    MDC.put("eventType", eventType);
    MDC.put("result", result);
  }

  private static String getClientIp(HttpServletRequest request) {
    if (request == null) {
      return "N/A";
    }
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty()) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  private static String generateCorrelationId() {
    return java.util.UUID.randomUUID().toString();
  }
}
