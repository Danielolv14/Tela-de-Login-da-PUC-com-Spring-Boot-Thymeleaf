package br.puc.secureaccess.controller;

import br.puc.secureaccess.dto.RegisterForm;
import br.puc.secureaccess.dto.ResetPasswordForm;
import br.puc.secureaccess.service.PasswordResetService;
import br.puc.secureaccess.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordResetService resetService;

    public AuthController(UserService userService, PasswordResetService resetService) {
        this.userService = userService;
        this.resetService = resetService;
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        return isLogged(auth) ? "redirect:/" : "login";
    }

    @GetMapping("/register")
    public String registerForm(Authentication auth, Model model) {
        if (isLogged(auth)) {
            return "redirect:/";
        }
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result,
                           RedirectAttributes redirect) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "As senhas não coincidem.");
        }
        if (!result.hasFieldErrors("username") && userService.usernameTaken(form.getUsername())) {
            result.rejectValue("username", "duplicate", "Este usuário já está em uso.");
        }
        if (!result.hasFieldErrors("email") && userService.emailTaken(form.getEmail())) {
            result.rejectValue("email", "duplicate", "Este email já está cadastrado.");
        }
        if (result.hasErrors()) {
            return "register";
        }
        userService.register(form);
        redirect.addFlashAttribute("success", "Cadastro realizado! Faça login para continuar.");
        return "redirect:/login";
    }

    @GetMapping("/recoverpassword")
    public String recoverForm() {
        return "recoverpassword";
    }

    @PostMapping("/recoverpassword")
    public String recover(@RequestParam("email") String email, Model model) {
        if (!email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            model.addAttribute("error", "Informe um email válido.");
            return "recoverpassword";
        }
        resetService.requestReset(email);
        model.addAttribute("success",
                "Se o email estiver cadastrado, enviaremos um link para redefinir a senha.");
        return "recoverpassword";
    }

    @GetMapping("/resetpassword")
    public String resetForm(@RequestParam(value = "token", required = false) String token, Model model) {
        if (!resetService.isValid(token)) {
            model.addAttribute("invalid", true);
            return "resetpassword";
        }
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);
        model.addAttribute("form", form);
        return "resetpassword";
    }

    @PostMapping("/resetpassword")
    public String reset(@Valid @ModelAttribute("form") ResetPasswordForm form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "As senhas não coincidem.");
        }
        if (result.hasErrors()) {
            return "resetpassword";
        }
        if (!resetService.resetPassword(form.getToken(), form.getPassword())) {
            model.addAttribute("invalid", true);
            return "resetpassword";
        }
        redirect.addFlashAttribute("success", "Senha redefinida! Faça login com a nova senha.");
        return "redirect:/login";
    }

    private boolean isLogged(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}
