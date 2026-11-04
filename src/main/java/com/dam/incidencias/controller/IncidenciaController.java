package com.dam.incidencias.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dam.incidencias.domain.Incidencia;
import com.dam.incidencias.service.EstadoService;
import com.dam.incidencias.service.IncidenciaService;

import java.util.List;

@Controller
public class IncidenciaController {

    private final IncidenciaService incidenciaService;
    private final EstadoService estadoService;

    public IncidenciaController(IncidenciaService incidenciaService, EstadoService estadoService) {
        this.incidenciaService = incidenciaService;
        this.estadoService = estadoService;
    }

    // Root -> the incident list (guarded by AuthInterceptor)
    @GetMapping("/")
    public String home() {
        return "redirect:/Incidencia";
    }

    // List
    @GetMapping("/Incidencia")
    public String listIncidencia(Model model) {
        List<Incidencia> incidencias = incidenciaService.getAllIssues();
        model.addAttribute("incidencias", incidencias);
        return "Incidencia-list";
    }

    // New
    @GetMapping("/Incidencia-add")
    public String showAddForm(Model model) {
        model.addAttribute("incidencia", new Incidencia());
        model.addAttribute("estados", estadoService.getAllEstados());
        return "Incidencia-add";
    }

    // Save (INSERT or UPDATE depending on whether the id is set)
    @PostMapping("/Incidencia")
    public String guardar(Incidencia incidencia) {
        incidenciaService.saveIncidencia(incidencia);
        return "redirect:/Incidencia";
    }

    // Update form
    @GetMapping("/Incidencia/updateData/{id}")
    public String update(@PathVariable Long id, Model model) {
        Incidencia incidencia = incidenciaService.findID(id);
        model.addAttribute("incidencia", incidencia);
        model.addAttribute("estados", estadoService.getAllEstados());
        return "Incidencia-update";
    }

    // Delete
    @GetMapping("/Incidencia/deleteData/{id}")
    public String delete(@PathVariable Long id) {
        incidenciaService.deleteIncidencia(id);
        return "redirect:/Incidencia";
    }

    // ---------- Login (session-based) ----------

    @GetMapping("/LoginIncidencia")
    public String mostrarLogin() {
        return "Incidencia-login";
    }

    @PostMapping("/Incidencia/login")
    public String postLogin(
            @RequestParam(name = "Name") String name,
            @RequestParam(name = "Password") String password,
            HttpSession session) {
        if (incidenciaService.checkLogin(name, password)) {
            session.setAttribute("usuario", name);
            return "redirect:/Incidencia";
        }
        return "redirect:/LoginIncidencia?error";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/LoginIncidencia";
    }
}
