package complaint_system.complaint_system.config;

import complaint_system.complaint_system.model.User;
import complaint_system.complaint_system.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService(UserService users) {
        return email -> {
            User user = users.findByEmail(email);
            if (user == null) throw new UsernameNotFoundException("User not found");
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService details, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(details);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider provider,
                                           ObjectProvider<ClientRegistrationRepository> registrations,
                                           UserService users) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(provider)
            .authorizeHttpRequests(auth -> auth
                // ── OAuth2 redirect endpoints ────────────────────────────────
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // ── Public pages, assets and auth APIs ──────────────────────
                .requestMatchers(
                    "/", "/index.html", "/login.html", "/signup.html", "/admin-login.html",
                    "/styles.css", "/app.js", "/admin.js", "/login.js", "/signup.js",
                    "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**"
                ).permitAll()
                // ── Public data endpoints ────────────────────────────────────
                .requestMatchers(HttpMethod.GET,  "/api/departments/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/complaints").permitAll()
                // ── Complaint list requires login ────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/complaints/**").authenticated()
                // ── Role-protected endpoints ─────────────────────────────────
                .requestMatchers("/admin.html", "/api/users/**", "/api/departments/**",
                                 "/api/complaints/*/assign").hasRole("ADMIN")
                .requestMatchers("/api/complaints/*/status").hasAnyRole("OFFICER", "ADMIN")
                .anyRequest().authenticated()
            );

        ClientRegistrationRepository clientRegistrationRepository = registrations.getIfAvailable();
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth -> oauth
                .clientRegistrationRepository(clientRegistrationRepository)
                .loginPage("/login.html")
                .defaultSuccessUrl("/", true)
                .successHandler((request, response, authentication) -> {
                    OAuth2User googleUser = (OAuth2User) authentication.getPrincipal();
                    String email = googleUser.getAttribute("email");
                    String name  = googleUser.getAttribute("name");
                    if (email == null || email.isBlank()) {
                        response.sendRedirect("/login.html?error=google_email_missing");
                        return;
                    }
                    User user = users.findOrCreateGoogleCitizen(name == null ? email : name, email);
                    Authentication appAuth = new UsernamePasswordAuthenticationToken(
                            user.getEmail(), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                    ctx.setAuthentication(appAuth);
                    SecurityContextHolder.setContext(ctx);
                    HttpSession session = request.getSession(true);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", ctx);
                    response.sendRedirect("ADMIN".equals(user.getRole().name()) ? "/admin.html" : "/");
                })
                .failureUrl("/login.html?error=google_failed")
            );
        }

        return http.build();
    }
}
