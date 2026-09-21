package br.puc.secureaccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

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
}
