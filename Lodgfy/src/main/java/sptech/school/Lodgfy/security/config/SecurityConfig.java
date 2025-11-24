package sptech.school.Lodgfy.security.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sptech.school.Lodgfy.security.jwt.JwtAuthenticationFilter;
import sptech.school.Lodgfy.security.jwt.JwtService;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Públicos
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/hospedes/registrar").permitAll()
                .requestMatchers("/api/hospedes/login").permitAll()
                .requestMatchers("/api/orcamentos/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()

                // Chalés: apenas GET é público, PUT/PATCH/DELETE requerem autenticação
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/chales/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/chales/**").hasAnyRole("HOSPEDE", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/chales/**").hasAnyRole("HOSPEDE", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/chales/**").hasAnyRole("HOSPEDE", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/chales/**").hasAnyRole("HOSPEDE", "ADMIN")

                // Reservas: operações requerem autenticação
                .requestMatchers("/api/reservas/**").hasAnyRole("HOSPEDE", "ADMIN")

                // Autenticados
                .requestMatchers("/api/hospedes/**").hasAnyRole("HOSPEDE", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // Quando não autenticado, retornar 403 (Forbidden)
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Acesso negado. Token não fornecido ou inválido.\"}");
                })
                // Quando autenticado, mas sem permissão, retornar 403
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Acesso negado. Permissão insuficiente.\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:4200",
            "http://localhost:8081",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:4200",
            "http://127.0.0.1:8081"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
