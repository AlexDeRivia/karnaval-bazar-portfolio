package com.karnaval.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.karnaval.configuracion.StripeSettings;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping(path = "/shoopingCar")
public class ShoopingCarController {
	private final StripeSettings stripeSettings;

	public ShoopingCarController(StripeSettings stripeSettings) {
		this.stripeSettings = stripeSettings;
	}
	
	@GetMapping("/openCar")
	public String showShoopingCarView(Model model) {
		model.addAttribute("checkoutEnabled", stripeSettings.ready());
		return "shoopingCar/shoopingCarView";
	}
}
