package it.unisalento.pasproject.assignmentservice.security;


import it.unisalento.pasproject.assignmentservice.exceptions.AccessDeniedException;
import it.unisalento.pasproject.assignmentservice.exceptions.UserNotAuthorizedException;
import it.unisalento.pasproject.assignmentservice.service.UserCheckService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilities jwtUtilities ;

    @Autowired
    private UserCheckService userCheckService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String role = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtUtilities.extractUsername(jwt);
                role = jwtUtilities.extractRole(jwt);
            }else {
                throw new AccessDeniedException("Missing token");
            }
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid token: " + e.getMessage());
        }


        if (username != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetailsDTO user = this.userCheckService.loadUserByUsername(username);

            String userEmail;
            String userRole;

            // Se token valido e risposta del cqrs null, si assume che l'utente sia l'email del token
            if (user == null){
                userEmail = username;
                userRole = role;
            }else {
                userEmail = user.getEmail();
                userRole = user.getRole();
            }

            UserDetails userDetails = User.builder()
                    .username(userEmail) // Assume email is username
                    .password("") // Password field is not used in JWT authentication
                    .authorities(userRole) // Set roles or authorities from the UserDetailsDTO
                    .build();

            if (jwtUtilities.validateToken(jwt, userDetails, userRole) && userCheckService.isEnable(user.getEnabled())) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                throw new UserNotAuthorizedException("User not authorized");
            }
        }

        if ( SecurityContextHolder.getContext().getAuthentication() == null ) {
            throw new AccessDeniedException("No authentication found");
        }

        chain.doFilter(request, response);
    }

}