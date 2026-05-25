package com.agroya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Vistas del administrador */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping({"", "/"})
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/logistica")
    public String logistica() {
        return "admin/logistica";
    }
}