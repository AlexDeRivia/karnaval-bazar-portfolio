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

import com.karnaval.entidad.Empleado;
import com.karnaval.servicio.EmpleadoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    // GET: /empleado/index or /empleado
    @GetMapping({"/index", ""})
    public String indice(Model model) {
        model.addAttribute("listaEmpleados", empleadoService.listarTodos());
        return "empleado/empleadoIndex"; // Renders empleadoIndex.html with list of empleados
    }

    // GET: /empleado/nuevo
    @GetMapping("/nuevo")
    public String empleadoNuevoForm(Model model) {
        model.addAttribute("empleado", new Empleado());
        return "empleado/empleadoForm"; // Renders empleadoForm.html for new empleado form
    }

    // POST: /empleado/guardar
    @PostMapping("/guardar")
    public String empleadoNuevoProcesar(
            @Valid @ModelAttribute("empleado") Empleado empleado,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "empleado/empleadoForm"; // Shows form again with validation errors
        }

        if (empleado.getId() == null) {
            empleadoService.agregar(empleado); // Adds new empleado
        } else {
            empleadoService.actualizar(empleado); // Updates existing empleado
        }

        return "redirect:/empleado/index"; // Redirects to empleado index page after processing
    }

    // GET: /empleado/editar/{id}
    @GetMapping("/editar/{id}")
    public String empleadoEditarForm(Model model,
                                     @PathVariable("id") Long id) {
        Empleado buscado = empleadoService.buscar(id);
        model.addAttribute("empleado", buscado != null ? buscado : new Empleado());
        return "empleado/empleadoForm"; // Renders empleadoForm.html for editing existing empleado
    }

    // GET: /empleado/eliminar/{id}
    @PostMapping("/eliminar/{id}")
    public String empleadoEliminar(Model model,
                                   @PathVariable("id") Long id) {
        empleadoService.eliminar(id); // Deletes empleado by id
        return "redirect:/empleado/index"; // Redirects to empleado index page after deleting
    }
}
