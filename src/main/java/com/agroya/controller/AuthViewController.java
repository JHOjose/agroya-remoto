package com.agroya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador MVC (Thymeleaf) para autenticación.
 * El login real ocurre via fetch() → POST /api/auth/signin (REST).
 * Este controlador solo sirve las páginas HTML.
 */
@Controller
public class AuthViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // → templates/auth/login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register"; // → templates/auth/register.html
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "auth/logout-success";
    
    }
}