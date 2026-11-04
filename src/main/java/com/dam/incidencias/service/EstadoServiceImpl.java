package com.dam.incidencias.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dam.incidencias.dao.EstadoDAO;
import com.dam.incidencias.domain.Estado;
import com.dam.incidencias.exception.EstadoNotFoundException;

@Service
public class EstadoServiceImpl implements EstadoService {

    private final EstadoDAO estadoDAO;

    public EstadoServiceImpl(EstadoDAO estadoDAO) {
        this.estadoDAO = estadoDAO;
    }

    @Override
    public List<Estado> getAllEstados() {
        return estadoDAO.findAll();
    }

    @Override
    public Estado getEstadoById(Long id) {
        return estadoDAO.findById(id).orElseThrow(() -> new EstadoNotFoundException(id));
    }

    @Override
    public Estado saveEstado(Estado estado) {
        return estadoDAO.save(estado);
    }

    @Override
    public void deleteEstado(Long id) {
        if (!estadoDAO.existsById(id)) {
            throw new EstadoNotFoundException(id);
        }
        estadoDAO.deleteById(id);
    }
}
