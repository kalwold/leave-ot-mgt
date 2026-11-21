package com.emp.mng.security.jwt;

import com.emp.mng.security.service.JwtService;
import com.emp.mng.user.entity.User;
import com.emp.mng.user.repository.UserRepository;
import com.emp.mng.user.repository.UserRoleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
   private final UserRoleRepository userRoleRepository;

   @Override
   protected void doFilterInternal(
           HttpServletRequest request,
           HttpServletResponse response,
           FilterChain filterChain
   ) throws ServletException, IOException {

      String authHeader = request.getHeader("Authorization");

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
         filterChain.doFilter(request, response);
         return;
      }

      String token = authHeader.substring(7);


      try {
         // Validate JWT signature and expiration
         Claims claims = jwtService.extractAllClaims(token);
         System.out.println("JWT successfully parsed.");
         // Extract user ID and session version
         Number userIdClaim = claims.get("userId", Number.class);
         Number sessionVersionClaim = claims.get("sessionVersion", Number.class);

         if(userIdClaim == null || sessionVersionClaim == null){
            filterChain.doFilter(request , response);
            return;
         }

         Long userId = userIdClaim.longValue();
         int tokenSessionVersion = sessionVersionClaim.intValue();

         //Find user in database
         User user = userRepository.findById(userId).orElse(null);

//         if(user == null ||
//         !Boolean.TRUE.equals(user.getActive()) ||
//         user.getSessionVersion() == null ||
//         user.getSessionVersion() != tokenSessionVersion){
//            SecurityContextHolder.clearContext();
//
//            filterChain.doFilter(request, response);
         if (user == null) {
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401,\"message\":\"Invalid authentication token.\"}"
            );

            return;
         }

         if (!Boolean.TRUE.equals(user.getActive())) {
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401,\"message\":\"Your account is inactive. Contact the administrator.\"}"
            );
            return;
         }

         //Load current roles from database
         List<SimpleGrantedAuthority> authorities = userRoleRepository.findAllByUser_Id(userId)
                 .stream()
                 .map(userRole -> new SimpleGrantedAuthority(
                         "ROLE_" + userRole.getRole().getRoleCode()
                 )).toList();
         if (Boolean.TRUE.equals(user.getMustChangePassword())) {

            String requestUri = request.getRequestURI();

            boolean allowedEndpoint =
                    requestUri.equals("/api/users/me/change-password") || requestUri.equals("/api/auth/login")||
                            requestUri.equals("/api/auth/logout");

            if (!allowedEndpoint) {
               SecurityContextHolder.clearContext();

               response.setStatus(HttpServletResponse.SC_FORBIDDEN);
               response.setContentType("application/json");
               response.getWriter().write(
                       "{\"message\":\"You must change your password before accessing the system.\"}"
               );

               return;
            }
         }
         if(authorities.isEmpty()){
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
         }

         // Create authenticated user
         UsernamePasswordAuthenticationToken authentication =
                 new UsernamePasswordAuthenticationToken(
                         user.getUsername(),
                         null,
                         authorities
                 );
         SecurityContextHolder.getContext()
                 .setAuthentication(authentication);
      } catch(JwtException | IllegalArgumentException exception) {

         SecurityContextHolder.clearContext();
      }
      filterChain.doFilter(request, response);
   }
}