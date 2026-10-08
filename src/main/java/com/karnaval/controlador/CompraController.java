package com.karnaval.controlador;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.karnaval.entidad.Compra;
import com.karnaval.entidad.CompraDetalle;
import com.karnaval.entidad.Producto;
import com.karnaval.servicio.CompraService;
import com.karnaval.servicio.ProveedorService;
import com.karnaval.servicio.EmpleadoService;
import com.karnaval.servicio.ProductoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/compra")
public class CompraController {

    @Autowired
    private CompraService compraService;
    @Autowired
    private ProveedorService proveedorService;
    @Autowired
    private EmpleadoService empleadoService;
    @Autowired
    private ProductoService productoService;
    
    @GetMapping({"/index", ""})
    public String indice(Model model) {
        model.addAttribute("listaCompras", compraService.listarTodos());
        return "compra/compraIndex";
    }

    @GetMapping("/nuevo")
    public String compraNuevoForm(Model model) {
        Compra compra = new Compra();
        compra.setCompraDetalles(new ArrayList<>()); // Inicializar lista de detalles
        model.addAttribute("compra", compra);
        model.addAttribute("proveedores", proveedorService.listarTodos());
        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("productos", productoService.listarTodos());
        return "compra/compraForm";
    }

    @PostMapping("/guardar")
    public String guardarCompra(
            @Valid @ModelAttribute("compra") Compra compra,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("proveedores", proveedorService.listarTodos());
            model.addAttribute("empleados", empleadoService.listarTodos());
            model.addAttribute("productos", productoService.listarTodos());
            return "compra/compraForm";
        }

        // Recuperar productos gestionados por la sesión antes de asignarlos a los detalles
        for (CompraDetalle detalle : compra.getCompraDetalles()) {
            Producto productoGestionado = productoService.buscar(detalle.getProducto().getId());
            if (productoGestionado != null) {
                detalle.setProducto(productoGestionado);
            }
            detalle.setCompra(compra);
        }

        if (compra.getId() == null) {
            compraService.agregar(compra);
        } else {
            compraService.actualizar(compra);
        }

        return "redirect:/compra/index";
    }
    
    @GetMapping("/ver/{id}")
    public String verCompra(@PathVariable Long id, Model model) {
        Compra compra = compraService.buscar(id);
        model.addAttribute("compra", compra);
        return "compra/verCompra";
    }

    @GetMapping("/editar/{id}")
    public String editarCompraForm(@PathVariable("id") Long id, Model model) {
        Compra compra = compraService.buscar(id);
        if (compra != null) {
            model.addAttribute("compra", compra);
            model.addAttribute("proveedores", proveedorService.listarTodos());
            model.addAttribute("empleados", empleadoService.listarTodos());
            model.addAttribute("productos", productoService.listarTodos());
            return "compra/compraForm";
        }
        return "redirect:/compra/index";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCompra(@PathVariable("id") Long id) {
        compraService.eliminar(id);
        return "redirect:/compra/index";
    }
}
