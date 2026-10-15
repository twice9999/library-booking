package ru.library.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Укажите логин")
    @Size(min = 3, max = 50, message = "От 3 до 50 символов")
    @Pattern(regexp = "[a-zA-Z0-9_.-]+", message = "Только латиница, цифры и _ . -")
    private String username;

    @NotBlank(message = "Укажите имя")
    @Size(max = 100, message = "Не длиннее 100 символов")
    private String fullName;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 4, max = 100, message = "Минимум 4 символа")
    private String password;

    @NotBlank(message = "Повторите пароль")
    private String confirmPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
