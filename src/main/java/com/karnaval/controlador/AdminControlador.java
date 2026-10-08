package com.karnaval.controlador;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.karnaval.repositorio.OnlineOrderRepository;
import com.karnaval.servicio.OnlineOrderService;




@Controller
@RequestMapping("/admin")
public class AdminControlador {
	private final OnlineOrderRepository orders;
	private final OnlineOrderService orderService;

	public AdminControlador(OnlineOrderRepository orders, OnlineOrderService orderService) {
		this.orders = orders;
		this.orderService = orderService;
	}
	
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

	@GetMapping("/pedidos")
	public String orders(@RequestParam(defaultValue = "0") int page, Model model) {
		model.addAttribute("orders", orders.findAll(PageRequest.of(Math.max(0, Math.min(page, 10000)),
				20, Sort.by(Sort.Direction.DESC, "createdAt"))));
		return "admin/orders";
	}

	@GetMapping("/pedidos/{id}")
	public String order(@PathVariable String id, Model model) {
		model.addAttribute("order", orderService.required(id));
		return "admin/orderDetail";
	}
	
}
