package br.puc.secureaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
@ExtendWith(OutputCaptureExtension.class)
class AuthFlowTest {

    @Autowired
    MockMvc mvc;

    @Test
    void publicPagesAndProtectedRedirect() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/register")).andExpect(status().isOk());
        mvc.perform(get("/recoverpassword")).andExpect(status().isOk());
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void registerValidatesAndAuthenticates() throws Exception {
        mvc.perform(post("/register").with(csrf())
                .param("username", "maria").param("email", "x")
                .param("password", "abc12345").param("confirmPassword", "outra123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "email", "confirmPassword"));

        mvc.perform(post("/register").with(csrf())
                .param("username", "maria").param("email", "maria@teste.com")
                .param("password", "abc12345").param("confirmPassword", "abc12345"))
                .andExpect(redirectedUrl("/login"));

        mvc.perform(post("/register").with(csrf())
                .param("username", "MARIA").param("email", "maria@teste.com")
                .param("password", "abc12345").param("confirmPassword", "abc12345"))
                .andExpect(model().attributeHasFieldErrors("form", "username", "email"));

        mvc.perform(formLogin().user("maria").password("abc12345")).andExpect(authenticated());
        mvc.perform(formLogin().user("maria@teste.com").password("abc12345")).andExpect(authenticated());
        mvc.perform(formLogin().user("maria").password("errada123"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void recoverPasswordDoesNotLeakUnknownEmail() throws Exception {
        mvc.perform(post("/recoverpassword").with(csrf()).param("email", "ninguem@teste.com"))
                .andExpect(status().isOk()).andExpect(model().attributeExists("success"));
        mvc.perform(get("/resetpassword").param("token", "invalido"))
                .andExpect(model().attribute("invalid", true));
    }

    @Test
    void resetWithoutSmtpPrintsLinkAndChangesPassword(CapturedOutput output) throws Exception {
        mvc.perform(post("/register").with(csrf())
                .param("username", "joao").param("email", "joao@teste.com")
                .param("password", "abc12345").param("confirmPassword", "abc12345"))
                .andExpect(redirectedUrl("/login"));

        // sem MAIL_HOST o link vai para o log do servidor
        mvc.perform(post("/recoverpassword").with(csrf()).param("email", "joao@teste.com"))
                .andExpect(model().attributeExists("success"));
        Matcher link = Pattern.compile("resetpassword\\?token=([A-Za-z0-9_-]+)").matcher(output.getOut());
        assertTrue(link.find(), "o link de recuperação deveria aparecer no log");
        String token = link.group(1);

        mvc.perform(get("/resetpassword").param("token", token))
                .andExpect(status().isOk()).andExpect(model().attributeExists("form"));
        mvc.perform(post("/resetpassword").with(csrf())
                .param("token", token).param("password", "nova12345").param("confirmPassword", "nova12345"))
                .andExpect(redirectedUrl("/login"));

        // o link é de uso único e a senha antiga deixa de valer
        mvc.perform(get("/resetpassword").param("token", token)).andExpect(model().attribute("invalid", true));
        mvc.perform(formLogin().user("joao").password("abc12345")).andExpect(unauthenticated());
        mvc.perform(formLogin().user("joao").password("nova12345")).andExpect(authenticated());
    }
}
