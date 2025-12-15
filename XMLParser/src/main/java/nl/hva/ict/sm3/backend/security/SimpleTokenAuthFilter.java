package nl.hva.ict.sm3.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class SimpleTokenAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SimpleTokenAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim(); // verwacht: user-123 of user-123-....
            Long userId = parseUserId(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    // principal = email, zodat jouw code auth.getName() -> email blijft werken
                    var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(request, response);
    }

    private Long parseUserId(String token) {
        try {
            if (!token.startsWith("user-")) return null;

            // ondersteunt zowel "user-123" als "user-123-<timestamp>"
            String rest = token.substring(5);
            String idPart = rest.contains("-") ? rest.substring(0, rest.indexOf('-')) : rest;

            return Long.parseLong(idPart);
        } catch (Exception e) {
            return null;
        }
    }
}
