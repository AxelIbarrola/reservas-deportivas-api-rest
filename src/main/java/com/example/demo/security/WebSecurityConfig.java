package com.example.demo.security;

import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.TokenRevocationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class WebSecurityConfig {
    private final CustomUserDetailsService userDetails;
    private final TokenRevocationService revocacion;
    private final JwtUtil jwtUtil;
    private final AuthEntryPointJwt noAutorizado;

    public WebSecurityConfig(CustomUserDetailsService userDetails, TokenRevocationService revocacion,
                             JwtUtil jwtUtil, AuthEntryPointJwt noAutorizado) {
        this.userDetails = userDetails;
        this.revocacion = revocacion;
        this.jwtUtil = jwtUtil;
        this.noAutorizado = noAutorizado;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        // Agregar aquí otros orígenes si Live Server usa un puerto distinto.
        cors.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5500",
                                       "http://127.0.0.1:5501", "http://localhost:5501"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API stateless: JWT en Authorization, nunca cookie de sesión.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(noAutorizado))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(aut -> aut
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/signin", "/api/auth/signup").permitAll()
                .requestMatchers("/api/test/all", "/h2-console", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/productos", "/productos/*").permitAll()
                .requestMatchers("/productos", "/productos/**").hasRole("ADMIN")
                .requestMatchers("/clientes/me").authenticated()
                .requestMatchers("/clientes", "/clientes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/facturas").hasRole("ADMIN")
                .requestMatchers("/facturas", "/facturas/**").authenticated()
                .anyRequest().authenticated()
            );
        http.addFilterBefore(new AuthTokenFilter(jwtUtil, userDetails, revocacion),
                             UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
