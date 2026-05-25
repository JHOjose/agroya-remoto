package com.agroya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Vistas del productor: sus productos + formulario crear/editar */
@Controller
public class ProductorViewController {

    @GetMapping("/mis-productos")
    public String misProductos() {
        return "productor/mis-productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevo() {
        return "productor/formulario";
    }

    @GetMapping("/productos/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("productoId", id);
        model.addAttribute("modoEdicion", true);
        return "productor/formulario";
    }
}