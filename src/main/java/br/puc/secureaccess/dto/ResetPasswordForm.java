package br.puc.secureaccess.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ResetPasswordForm {

    private String token;

    @NotBlank(message = "Informe a nova senha.")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "A senha deve conter letras e números.")
    private String password;

    @NotBlank(message = "Confirme a senha.")
    private String confirmPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
