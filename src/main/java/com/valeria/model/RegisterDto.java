package com.valeria.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto
{

    // DTO для регистрации пользователя
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 30, message = "Имя пользователя должно быть от 3 до 30 символов")
    private String username;

    @Email(message = "Некорректный email")
    @NotBlank(message = "email обязателен")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Пароль должен содержать: минимум 1 цифру, 1 строчную и 1 заглавную букву, 1 специальный символ (!@#$%^&+=)"
    )
    private String password;

    private String role; // Роль по умолчанию
}
