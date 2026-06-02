package com.shoestore.backend.validation.passwordvalidator;

import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String password;
    private String repeatedPassword;

    @Override
    public boolean isValid(Object requestDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (requestDto instanceof UserUpdateRequestDto dto) {
            password = dto.password();
            repeatedPassword = dto.repeatedPassword();
        }
        if (requestDto instanceof UserRegistrationRequestDto dto) {
            password = dto.password();
            repeatedPassword = dto.repeatedPassword();
        }
        if (password == null && repeatedPassword == null) {
            return true;
        }
        if (password == null || repeatedPassword == null) {
            return false;
        }
        return password.equals(repeatedPassword);
    }
}
