package com.dam.incidencias.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.service.EstadoService;

@Controller
public class EstadoController {

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

    // ---------- CREAR ----------

    @GetMapping("/Estado-add")
    public String showAddEstadoForm(Model model) {
        model.addAttribute("estado", new Estado());
        return "Estado-add";
    }

    @PostMapping("/Estado")
    public String saveEstado(@ModelAttribute Estado estado) {
        estadoService.saveEstado(estado);
        return "redirect:/Estado-list";
    }

    // ---------- LISTAR ----------

    @GetMapping("/Estado-list")
    public String listEstados(Model model) {
        model.addAttribute("estados", estadoService.getAllEstados());
        return "Estado-list";
    }

    // ---------- EDITAR ----------

    @GetMapping("/Estado-edit/{id}")
    public String showEditEstadoForm(@PathVariable Long id, Model model) {
        Estado estado = estadoService.getEstadoById(id);

        if (estado != null) {
            model.addAttribute("estado", estado);
            return "Estado-edit";
        } else {
            return "redirect:/Estado-list";
        }
    }

    @PostMapping("/Estado-update")
    public String updateEstado(@ModelAttribute("estado") Estado estado) {
        estadoService.saveEstado(estado); // save() hace INSERT o UPDATE según tenga id o no
        return "redirect:/Estado-list";
    }

    // ---------- ELIMINAR ----------

    @GetMapping("/Estado-delete/{id}")
    public String deleteEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            estadoService.deleteEstado(id);
            redirectAttributes.addFlashAttribute("mensaje", "Estado eliminado correctamente.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede eliminar este estado porque hay incidencias que lo están usando.");
        }
        return "redirect:/Estado-list";
    }
}
