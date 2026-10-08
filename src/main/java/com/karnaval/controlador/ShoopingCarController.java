package com.karnaval.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping(path = "/shoopingCar")
public class ShoopingCarController {
	@Value("${app.checkout.mode:disabled}")
	private String checkoutMode;
	
	@GetMapping("/openCar")
	public String showShoopingCarView(Model model) {
		model.addAttribute("checkoutEnabled", !"disabled".equals(checkoutMode));
		return "shoopingCar/shoopingCarView";
	}
}
