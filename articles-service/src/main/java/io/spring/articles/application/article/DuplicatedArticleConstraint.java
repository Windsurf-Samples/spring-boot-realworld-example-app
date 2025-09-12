package io.spring.articles.application.article;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = DuplicatedArticleValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicatedArticleConstraint {
  String message() default "article name exists";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
