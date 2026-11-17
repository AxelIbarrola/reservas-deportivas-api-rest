package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.repository.RevokedTokenRepository;
import com.example.demo.security.JwtUtil;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:logouttests;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class LogoutIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired RevokedTokenRepository revocados;

    @Test
    void jwtSeRevocaEnLogoutPeroElNuevoLoginFunciona() throws Exception {
        String usuario = "test-" + UUID.randomUUID();
        String password = "claveSegura123";
        String registro = """
          {"username":"%s","password":"%s","nombre":"Test","apellido":"Demo","email":"%s@ejemplo.com"}
          """.formatted(usuario, password, usuario);
        String login = """ 
          {"username":"%s","password":"%s"}
          """.formatted(usuario, password);

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(registro))
            .andExpect(status().isCreated());

        String jwt1 = obtenerToken(login);
        String jti1 = jwtUtil.getJtiFromToken(jwt1);
        assertFalse(revocados.existsById(jti1));

        mvc.perform(get("/api/test/user").header("Authorization", "Bearer " + jwt1))
            .andExpect(status().isOk());
        mvc.perform(get("/api/test/user").header("Authorization", "Bearer token-invalido"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + jwt1))
            .andExpect(status().isOk());
        assertTrue(revocados.existsById(jti1));

        mvc.perform(get("/api/test/user").header("Authorization", "Bearer " + jwt1))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + jwt1))
            .andExpect(status().isUnauthorized());

        String jwt2 = obtenerToken(login);
        assertNotEquals(jwt1, jwt2);
        assertNotEquals(jti1, jwtUtil.getJtiFromToken(jwt2));
        mvc.perform(get("/api/test/user").header("Authorization", "Bearer " + jwt2))
            .andExpect(status().isOk());
    }

    private String obtenerToken(String login) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON).content(login))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.token");
    }
}
