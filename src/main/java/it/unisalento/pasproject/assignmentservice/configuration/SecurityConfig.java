package it.unisalento.pasproject.assignmentservice.configuration;

import it.unisalento.pasproject.assignmentservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true,securedEnabled = true)
public class SecurityConfig {
    /**
     * Configures the security filter chain for the application.
     * <p>
     * This method is used to configure the security settings for the application. It sets up the authorization rules,
     * session management policy, and Cross-Origin Resource Sharing (CORS) and Cross-Site Request Forgery (CSRF) settings.
     *
     * @param http The HttpSecurity object to be configured.
     * @return A SecurityFilterChain object that contains the security settings for the application.
     * @throws Exception If an error occurs during the configuration process.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/tasks/assignment/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Configurazione CORS
        http.cors(AbstractHttpConfigurer::disable); // Disabilita CORS

        // Configurazione CSRF
        http.csrf(AbstractHttpConfigurer::disable); // Disabilita CSRF

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

}