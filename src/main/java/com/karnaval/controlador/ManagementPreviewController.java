package com.karnaval.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ManagementPreviewController {
    @GetMapping("/gestion")
    public String overview(HttpServletResponse response) {
        // The public view is intentionally independent of repositories and customer records.
        response.setHeader("Cache-Control", "public, max-age=300");
        return "gestion/overview";
    }
}
