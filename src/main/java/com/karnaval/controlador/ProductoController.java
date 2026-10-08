package com.karnaval.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.karnaval.entidad.Producto;
import com.karnaval.servicio.ProductoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // GET: /producto/index or /producto
    @GetMapping({"/index", ""})
    public String indice(Model model) {
        model.addAttribute("listaProductos", productoService.listarTodos());
        return "producto/productoIndex"; // Renders productoIndex.html with list of productos
    }

    // GET: /producto/nuevo
    @GetMapping("/nuevo")
    public String productoNuevoForm(Model model) {
        model.addAttribute("producto", new Producto());
        return "producto/productoForm"; // Renders productoForm.html for new producto form
    }

    // POST: /producto/guardar
    @PostMapping("/guardar")
    public String productoNuevoProcesar(
            @Valid @ModelAttribute("producto") Producto producto,
            BindingResult bindingResult,
            Model model,
            @RequestParam(required = false) Integer expectedStock) {
        model.addAttribute("expectedStock", expectedStock);
        if (bindingResult.hasErrors()) {
            return "producto/productoForm"; // Shows form again with validation errors
        }

        if (producto.getId() == null) {
            productoService.agregar(producto); // Adds new producto
        } else {
            if (expectedStock == null || !productoService.actualizarSiStockCoincide(producto, expectedStock)) {
                model.addAttribute("saveError", "El stock cambió mientras editabas. Recarga el producto y vuelve a intentarlo.");
                return "producto/productoForm";
            }
        }

        return "redirect:/producto/index"; // Redirects to producto index page after processing
    }

    // GET: /producto/editar/{id}
    @GetMapping("/editar/{id}")
    public String productoEditarForm(Model model,
                                     @PathVariable("id") Long id) {
        Producto buscado = productoService.buscar(id);
        if (buscado == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("producto", buscado);
        model.addAttribute("expectedStock", buscado.getStock());
        return "producto/productoForm"; // Renders productoForm.html for editing existing producto
    }

    // GET: /producto/eliminar/{id}
    @PostMapping("/eliminar/{id}")
    public String productoEliminar(Model model,
                                   @PathVariable("id") Long id) {
        productoService.eliminar(id); // Deletes producto by id
        return "redirect:/producto/index"; // Redirects to producto index page after deleting
    }
}
