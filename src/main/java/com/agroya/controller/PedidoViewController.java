package com.agroya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** Vistas del comprador: lista de pedidos + detalle */
@Controller
@RequestMapping("/pedidos")
public class PedidoViewController {

    @GetMapping("/mis-pedidos")
    public String misPedidos() {
        return "pedidos/mis-pedidos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("pedidoId", id);
        return "pedidos/detalle-pedido";
    }
}