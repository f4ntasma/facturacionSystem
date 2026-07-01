package com.facturacion.cliente;

import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.empresa.Empresa;
import com.facturacion.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteResponse> listar(User user) {
        Empresa empresa = requireEmpresa(user);
        return clienteRepository.findByEmpresa(empresa).stream()
                .map(ClienteResponse::new)
                .toList();
    }

    public ClienteResponse obtener(User user, UUID id) {
        Empresa empresa = requireEmpresa(user);
        Cliente c = clienteRepository.findById(id)
                .filter(cl -> cl.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        return new ClienteResponse(c);
    }

    @Transactional
    public ClienteResponse crear(User user, ClienteRequest request) {
        Empresa empresa = requireEmpresa(user);
        Cliente c = new Cliente();
        c.setEmpresa(empresa);
        copy(request, c);
        return new ClienteResponse(clienteRepository.save(c));
    }

    @Transactional
    public ClienteResponse actualizar(User user, UUID id, ClienteRequest request) {
        Empresa empresa = requireEmpresa(user);
        Cliente c = clienteRepository.findById(id)
                .filter(cl -> cl.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        copy(request, c);
        return new ClienteResponse(clienteRepository.save(c));
    }

    @Transactional
    public void eliminar(User user, UUID id) {
        Empresa empresa = requireEmpresa(user);
        Cliente c = clienteRepository.findById(id)
                .filter(cl -> cl.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        clienteRepository.delete(c);
    }

    private void copy(ClienteRequest request, Cliente c) {
        c.setNombre(request.getNombre());
        c.setApellido(request.getApellido());
        c.setDni(request.getDni());
        c.setTelefono(request.getTelefono());
        c.setDireccion(request.getDireccion());
        c.setEmail(request.getEmail());
    }

    private Empresa requireEmpresa(User user) {
        if (user.getEmpresa() == null) {
            throw new BadRequestException("El usuario no tiene empresa asociada");
        }
        return user.getEmpresa();
    }
}