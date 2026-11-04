package com.dam.incidencias.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dam.incidencias.dao.EstadoDAO;
import com.dam.incidencias.dao.IncidenciaDAO;
import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.domain.Incidencia;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

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

        boolean esNueva = (incidencia.getId() == null);

        if (esNueva) {
            // Nueva incidencia
            incidencia.setOpenedAt(LocalDateTime.now());

            // Si no viene estado del formulario, ponemos por defecto ABIERTO
            if (incidencia.getStatus() == null || incidencia.getStatus().getId() == null) {
                Estado estadoAbierto = estadoDAO.findById(Estado.OPEN_ID).orElse(null);
                incidencia.setStatus(estadoAbierto);
            }

        } else {
            // UPDATE → recuperamos la original para no perder la fecha de apertura
            Incidencia original = incidenciaDAO.findById(incidencia.getId()).orElse(null);

            if (original != null) {
                incidencia.setOpenedAt(original.getOpenedAt());
            }
            // OJO: aquí NO tocamos incidencia.setStatus(...)
            // porque ya viene del formulario con el nuevo estado
        }

        // Si el estado es CERRADO, calculamos tiempos
        if (isClosed(incidencia.getStatus())) {

            incidencia.setClosedAt(LocalDateTime.now());

            incidencia.setResolutionMinutes(
                    Duration.between(incidencia.getOpenedAt(), incidencia.getClosedAt()).toMinutes());
        } else {
            // Si NO está cerrada, por si acaso dejamos estos a null
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
        return incidenciaDAO.findById(id).orElse(null);
    }

    @Override
    public void deleteIncidencia(Long id) {
        incidenciaDAO.deleteById(id);
    }

    // Demo credentials — there is no user table, this is the only account.
    private static final String DEMO_USER = "admin";
    private static final String DEMO_PASSWORD = "admin";

    @Override
    public boolean checkLogin(String name, String password) {
        return DEMO_USER.equals(name) && DEMO_PASSWORD.equals(password);
    }
}
