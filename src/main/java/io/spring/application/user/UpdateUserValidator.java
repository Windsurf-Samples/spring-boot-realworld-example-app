package io.spring.application.user;

import io.spring.core.user.UserRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UpdateUserValidator
    implements ConstraintValidator<UpdateUserConstraint, UpdateUserCommand> {

  private UserRepository userRepository;

  @Override
  public boolean isValid(UpdateUserCommand value, ConstraintValidatorContext context) {
    if (value.getParam().getEmail() != null && !value.getParam().getEmail().isEmpty()) {
      return !userRepository.findByEmail(value.getParam().getEmail()).isPresent()
          || userRepository
              .findByEmail(value.getParam().getEmail())
              .get()
              .getId()
              .equals(value.getTargetUser().getId());
    }
    if (value.getParam().getUsername() != null && !value.getParam().getUsername().isEmpty()) {
      return !userRepository.findByUsername(value.getParam().getUsername()).isPresent()
          || userRepository
              .findByUsername(value.getParam().getUsername())
              .get()
              .getId()
              .equals(value.getTargetUser().getId());
    }
    return true;
  }
}
