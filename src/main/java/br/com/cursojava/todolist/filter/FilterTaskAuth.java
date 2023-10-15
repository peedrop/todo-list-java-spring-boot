package br.com.cursojava.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.cursojava.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Checking if request is to create a task
        var requestURI = request.getRequestURI();
        if (requestURI.equals("/users/create")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Getting authorization header
        var authorization = request.getHeader("Authorization");
        authorization = authorization.substring("Basic".length()).trim();
        authorization = new String(Base64.getDecoder().decode(authorization));
        String[] credentials = authorization.split(":");
        String username = credentials[0];
        String password = credentials[1];

        // Checking if user exists
        var user = this.userRepository.findByUsername(username);
        if (user == null) {
            response.sendError(401);
            return;
        }

        // Checking if password is correct
        var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());
        if (!passwordVerify.verified) {
            response.sendError(401);
            return;
        }

        // Allowing request
        request.setAttribute("idUser", user.getId());
        filterChain.doFilter(request, response);
    }
}
