package com.karnaval.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/message-responses")
public class PaymentGatewayController {

	@GetMapping("/openSuccess")
	public String showSuccesView(Model model) {
		return "message-responses/success";
	}
	
	@GetMapping("/openCancel")
	public String showCancelView(Model model) {
		return "message-responses/cancel";
	}
}
