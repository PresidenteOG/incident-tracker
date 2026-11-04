package com.dam.incidencias.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dam.incidencias.dao.EstadoDAO;
import com.dam.incidencias.dao.IncidenciaDAO;
import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.domain.Incidencia;
import com.dam.incidencias.exception.EstadoNotFoundException;
import com.dam.incidencias.exception.IncidenciaNotFoundException;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

    // Demo credentials — there is no user table, this is the only account.
    private static final String DEMO_USER = "admin";
    private static final String DEMO_PASSWORD = "admin";

    private final IncidenciaDAO incidenciaDAO;
    private final EstadoDAO estadoDAO;

    public IncidenciaServiceImpl(IncidenciaDAO incidenciaDAO, EstadoDAO estadoDAO) {
        this.incidenciaDAO = incidenciaDAO;
        this.estadoDAO = estadoDAO;
    }

    @Override
    public List<Incidencia> getAllIssues() {
        return incidenciaDAO.findAll();
    }

    @Override
    public Incidencia saveIncidencia(Incidencia incidencia) {
        if (incidencia.getId() == null) {
            incidencia.setOpenedAt(LocalDateTime.now());

            // No status from the form: a new incident starts open.
            if (incidencia.getStatus() == null || incidencia.getStatus().getId() == null) {
                incidencia.setStatus(estadoDAO.findById(Estado.OPEN_ID)
                        .orElseThrow(() -> new EstadoNotFoundException(Estado.OPEN_ID)));
            }
        } else {
            // The form does not carry the opening time, so take it from the stored row.
            Incidencia original = findID(incidencia.getId());
            incidencia.setOpenedAt(original.getOpenedAt());
        }

        if (isClosed(incidencia.getStatus())) {
            incidencia.setClosedAt(LocalDateTime.now());
            incidencia.setResolutionMinutes(
                    Duration.between(incidencia.getOpenedAt(), incidencia.getClosedAt()).toMinutes());
        } else {
            incidencia.setClosedAt(null);
            incidencia.setResolutionMinutes(null);
        }

        return incidenciaDAO.save(incidencia);
    }

    private static boolean isClosed(Estado estado) {
        return estado != null && estado.getId() != null && estado.getId() == Estado.CLOSED_ID;
    }

    @Override
    public Incidencia findID(Long id) {
        return incidenciaDAO.findById(id).orElseThrow(() -> new IncidenciaNotFoundException(id));
    }

    @Override
    public void deleteIncidencia(Long id) {
        if (!incidenciaDAO.existsById(id)) {
            throw new IncidenciaNotFoundException(id);
        }
        incidenciaDAO.deleteById(id);
    }

    @Override
    public boolean checkLogin(String name, String password) {
        return DEMO_USER.equals(name) && DEMO_PASSWORD.equals(password);
    }
}
