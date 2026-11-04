package com.dam.incidencias.service;

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
            incidencia.setData_obertura(LocalDateTime.now());

            // Si no viene estado del formulario, ponemos por defecto ABIERTO
            if (incidencia.getEstado() == null || incidencia.getEstado().getId() == null) {
                Estado estadoAbierto = estadoDAO.findById(Estado.OPEN_ID).orElse(null);
                incidencia.setEstado(estadoAbierto);
            }

        } else {
            // UPDATE → recuperamos la original para no perder la fecha de apertura
            Incidencia original = incidenciaDAO.findById(incidencia.getId()).orElse(null);

            if (original != null) {
                incidencia.setData_obertura(original.getData_obertura());
            }
            // OJO: aquí NO tocamos incidencia.setEstado(...)
            // porque ya viene del formulario con el nuevo estado
        }

        // Si el estado es CERRADO, calculamos tiempos
        if (isClosed(incidencia.getEstado())) {

            incidencia.setData_tancament(LocalDateTime.now());

            long minutosTotales = java.time.Duration
                    .between(incidencia.getData_obertura(), incidencia.getData_tancament())
                    .toMinutes();

            long horas = minutosTotales / 60;
            long minutos = minutosTotales % 60;

            incidencia.setTemps_resolucio(horas + "h " + minutos + "m");
        } else {
            // Si NO está cerrada, por si acaso dejamos estos a null
            incidencia.setData_tancament(null);
            incidencia.setTemps_resolucio(null);
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
