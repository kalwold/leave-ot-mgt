package com.emp.mng.security.jwt;

import com.emp.mng.user.entity.User;
import com.emp.mng.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
   private final JwtService jwtService;
   private final UserRepository userRepository;

   @Override
   protected void doFilterInternal(
           HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
   ) throws ServletException, IOException {
      String authHeader = request.getHeader("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
         filterChain.doFilter(request, response);
         return;
      }
      String token = authHeader.substring(7);

      try {
         if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request,response);
         }
         String username = jwtService.extractUsername(token);
         if (SecurityContextHolder.getContext().getAuthentication() == null){
             User user = userRepository.findByUsername(username).orElse(null);

         if (user != null && Boolean.TRUE.equals(user.getActive())){
            var userRoles = user.getId() == null? List.<String>of() : userRepository.findById(user.getId())
                    .map(u -> u.getUsername())
                    .map(name ->List.of(name))
                    .orElse(List.of());

            var assignedRoles = userRoles.isEmpty() ? List.<SimpleGrantedAuthority>of()
                    : List.of(new SimpleGrantedAuthority("ROLE_"+userRepository.findByUsername(username)
                    .map(u ->"EMPLOYEE").orElse("EMPLOYEE")) );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null, assignedRoles
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
         }
         }

      } catch(Exception exception) {
         SecurityContextHolder.clearContext();
      }
      filterChain.doFilter(request, response);
   }
}