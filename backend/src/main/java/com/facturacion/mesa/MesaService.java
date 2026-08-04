package com.facturacion.mesa;

import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.empresa.Empresa;
import com.facturacion.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    public List<MesaResponse> listar(User user) {
        return mesaRepository.findByEmpresa(requireEmpresa(user))
                .stream().map(MesaResponse::new).toList();
    }

    @Transactional
    public MesaResponse crear(User user, MesaRequest request) {
        Mesa m = new Mesa();
        m.setEmpresa(requireEmpresa(user));
        m.setNombre(request.getNombre());
        m.setPiso(request.getPiso());
        if (request.getEstado() != null) m.setEstado(request.getEstado());
        return new MesaResponse(mesaRepository.save(m));
    }

    @Transactional
    public MesaResponse actualizar(User user, UUID id, MesaRequest request) {
        Mesa m = getMesa(user, id);
        m.setNombre(request.getNombre());
        m.setPiso(request.getPiso());
        if (request.getEstado() != null) m.setEstado(request.getEstado());
        return new MesaResponse(mesaRepository.save(m));
    }

    @Transactional
    public MesaResponse cambiarEstado(User user, UUID id, EstadoMesa estado) {
        Mesa m = getMesa(user, id);
        m.setEstado(estado);
        return new MesaResponse(mesaRepository.save(m));
    }

    @Transactional
    public void eliminar(User user, UUID id) {
        mesaRepository.delete(getMesa(user, id));
    }

    private Mesa getMesa(User user, UUID id) {
        Empresa empresa = requireEmpresa(user);
        return mesaRepository.findById(id)
                .filter(m -> m.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new NotFoundException("Mesa no encontrada"));
    }

    private Empresa requireEmpresa(User user) {
        if (user.getEmpresa() == null)
            throw new BadRequestException("El usuario no tiene empresa asociada");
        return user.getEmpresa();
    }
}