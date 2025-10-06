package com.example.proyectohorario_1.seguridad;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        HttpSession session = req.getSession(false);

        boolean loggedIn = (session != null && session.getAttribute("usuario") != null);

        boolean isLoginPage = uri.equals("/login") || uri.equals("/logout");

        if (loggedIn || isLoginPage || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/img")) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect("/login");
        }

    }
}