package com.karnaval.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;




@Controller
@RequestMapping("/admin")
public class AdminControlador {
	
	@GetMapping("/login")
	public String login(Model model) {
		return "Usuario/paginaLogin";
	}
	
	@GetMapping("/denegado")
	public String accesoDenegado(Authentication authResult, Model model) {
		
		String role = authResult == null ? "Ninguno" : authResult.getAuthorities().toString();
		model.addAttribute("roles", role);
		
		return "Usuario/pagina403";
	}
	
	@GetMapping({"/index", ""})
    public String getIndex(Model model) {
        return "admin/indexAdmin";
    }
	
}
