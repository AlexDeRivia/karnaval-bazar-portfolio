package com.karnaval.controlador;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.karnaval.repositorio.ProductoRepository;

@Controller
public class CatalogController {
    private final ProductoRepository productos;

    public CatalogController(ProductoRepository productos) {
        this.productos = productos;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("demo", true);
        return "index";
    }

    @GetMapping("/api/catalog")
    @ResponseBody
    public List<CatalogItem> catalog() {
        return productos.findAll().stream()
                .filter(producto -> producto.getStock() != null && producto.getStock() > 0)
                .map(producto -> new CatalogItem(producto.getId(), producto.getNombre(),
                        producto.getPrecio(), producto.getStock(), producto.getFoto(),
                        producto.getCategoria()))
                .toList();
    }

    public record CatalogItem(Long id, String nombre, BigDecimal precio, Integer stock,
            String foto, String categoria) {}
}
