package br.puc.secureaccess.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegisterForm {

    @NotBlank(message = "Informe o nome de usuário.")
    @Size(min = 3, max = 50, message = "O usuário deve ter entre 3 e 50 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "Use apenas letras, números, ponto, hífen e underline.")
    private String username;

    @NotBlank(message = "Informe o email.")
    @Email(message = "Email inválido.")
    @Size(max = 120, message = "Email muito longo.")
    private String email;

    @NotBlank(message = "Informe a senha.")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "A senha deve conter letras e números.")
    private String password;

    @NotBlank(message = "Confirme a senha.")
    private String confirmPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
