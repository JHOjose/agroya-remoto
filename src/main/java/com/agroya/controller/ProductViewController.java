package com.agroya.controller;

import com.agroya.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductService productService;

    @GetMapping
    public String catalogo(Model model,
                           @RequestParam(required = false) String categoria,
                           @RequestParam(required = false) String buscar) {
        model.addAttribute("categoriaFiltro", categoria);
        model.addAttribute("buscarFiltro", buscar);
        return "productos/catalogo";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("productoId", id);
        return "productos/detalle";
    }

}