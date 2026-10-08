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

import com.karnaval.entidad.Proveedor;
import com.karnaval.servicio.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/proveedor")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping({"/index", ""})
    public String indice(Model model) {
        model.addAttribute("listaProveedores", proveedorService.listarTodos());
        return "proveedor/proveedorIndex";
    }

    @GetMapping("/nuevo")
    public String proveedorNuevoForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedor/proveedorForm";
    }

    @PostMapping("/guardar")
    public String proveedorNuevoProcesar(
            @Valid @ModelAttribute("proveedor") Proveedor proveedor,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "proveedor/proveedorForm";
        }

        if (proveedor.getId() == null) {
            proveedorService.agregar(proveedor);
        } else {
            proveedorService.actualizar(proveedor);
        }

        return "redirect:/proveedor/index";
    }

    @GetMapping("/editar/{id}")
    public String proveedorEditarForm(Model model,
                                      @PathVariable("id") Long id) {
        Proveedor buscado = proveedorService.buscar(id);
        model.addAttribute("proveedor", buscado != null ? buscado : new Proveedor());
        return "proveedor/proveedorForm";
    }

    @PostMapping("/eliminar/{id}")
    public String proveedorEliminar(Model model,
                                    @PathVariable("id") Long id) {
        proveedorService.eliminar(id);
        return "redirect:/proveedor/index";
    }
}
