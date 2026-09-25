package complaint_system.complaint_system.config;

import complaint_system.complaint_system.model.User;
import complaint_system.complaint_system.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public UserDetailsService userDetailsService(UserService users) {
        return email -> {
            User user = users.findByEmail(email);
            if (user == null) throw new UsernameNotFoundException("User not found");
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        };
    }
    @Bean public DaoAuthenticationProvider authenticationProvider(UserDetailsService details, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(details); provider.setPasswordEncoder(encoder); return provider;
    }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }
    @Bean public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        return http.csrf(csrf -> csrf.disable()).authenticationProvider(provider).authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login.html", "/admin-login.html", "/styles.css", "/app.js", "/admin.js", "/login.js", "/api/auth/login", "/api/auth/logout", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments/**").authenticated()
                .requestMatchers("/admin.html", "/api/users/**", "/api/departments/**", "/api/complaints/*/assign").hasRole("ADMIN")
                .requestMatchers("/api/complaints/*/status").hasAnyRole("OFFICER", "ADMIN")
                .anyRequest().authenticated()).build();
    }
}
