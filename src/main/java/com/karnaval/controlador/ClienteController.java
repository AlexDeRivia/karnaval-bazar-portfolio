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

import com.karnaval.entidad.Cliente;
import com.karnaval.servicio.ClienteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // GET: /cliente/index or /cliente
    @GetMapping({"/index", ""})
    public String indice(Model model) {
        model.addAttribute("listaClientes", clienteService.listarTodos());
        return "cliente/clienteIndex"; // Renders clienteIndex.html with list of clientes
    }

    // GET: /cliente/nuevo
    @GetMapping("/nuevo")
    public String clienteNuevoForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cliente/clienteForm"; // Renders clienteForm.html for new cliente form
    }

    // POST: /cliente/guardar
    @PostMapping("/guardar")
    public String clienteNuevoProcesar(
            @Valid @ModelAttribute("cliente") Cliente cliente,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "cliente/clienteForm"; // Shows form again with validation errors
        }

        if (cliente.getId() == null) {
            clienteService.agregar(cliente); // Adds new cliente
        } else {
            clienteService.actualizar(cliente); // Updates existing cliente
        }

        return "redirect:/cliente/index"; // Redirects to cliente index page after processing
    }

    // GET: /cliente/editar/{id}
    @GetMapping("/editar/{id}")
    public String clienteEditarForm(Model model,
                                    @PathVariable("id") Long id) {
        Cliente buscado = clienteService.buscar(id);
        model.addAttribute("cliente", buscado != null ? buscado : new Cliente());
        return "cliente/clienteForm"; // Renders clienteForm.html for editing existing cliente
    }

    // GET: /cliente/eliminar/{id}
    @PostMapping("/eliminar/{id}")
    public String clienteEliminar(Model model,
                                  @PathVariable("id") Long id) {
        clienteService.eliminar(id); // Deletes cliente by id
        return "redirect:/cliente/index"; // Redirects to cliente index page after deleting
    }
}
