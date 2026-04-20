package com.pawlink.api.service;

import com.pawlink.api.dto.MascotaVirtualDTO;
import com.pawlink.api.model.Mascota;
import com.pawlink.api.model.MascotaVirtual;
import com.pawlink.api.repository.MascotaRepository;
import com.pawlink.api.repository.MascotaVirtualRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JuegoService {

    private static final int VALOR_MAX = 100;
    private static final int NIVEL_INICIAL = 1;
    private static final int EXPERIENCIA_INICIAL = 0;

    private final MascotaVirtualRepository mascotaVirtualRepository;
    private final MascotaRepository mascotaRepository;

    @Transactional
    public MascotaVirtualDTO obtenerOCrear(Integer idMascota) {
        return mascotaVirtualRepository.findByMascotaReal_IdMascota(idMascota)
                .map(this::toDTO)
                .orElseGet(() -> crearPorDefecto(idMascota));
    }

    @Transactional
    public MascotaVirtualDTO actualizar(Integer idMascota, MascotaVirtualDTO request) {
        MascotaVirtual virtual = mascotaVirtualRepository.findByMascotaReal_IdMascota(idMascota)
                .orElseGet(() -> nuevaParaMascota(idMascota));

        if (request.getNombre() != null) {
            virtual.setNombre(request.getNombre());
        } else if (virtual.getNombre() == null) {
            virtual.setNombre(virtual.getMascotaReal().getNombre());
        }
        if (request.getEspecie() != null) {
            virtual.setEspecie(request.getEspecie());
        }
        if (request.getNivel() != null) {
            virtual.setNivel(request.getNivel());
        }
        if (request.getExperiencia() != null) {
            virtual.setExperiencia(request.getExperiencia());
        }
        if (request.getHambre() != null) {
            virtual.setHambre(request.getHambre());
        }
        if (request.getFelicidad() != null) {
            virtual.setFelicidad(request.getFelicidad());
        }
        if (request.getEnergia() != null) {
            virtual.setEnergia(request.getEnergia());
        }
        if (request.getHigiene() != null) {
            virtual.setHigiene(request.getHigiene());
        }
        virtual.setUltimaInteraccion(LocalDateTime.now());

        return toDTO(mascotaVirtualRepository.save(virtual));
    }

    private MascotaVirtualDTO crearPorDefecto(Integer idMascota) {
        MascotaVirtual virtual = nuevaParaMascota(idMascota);
        virtual.setNombre(virtual.getMascotaReal().getNombre());
        virtual.setEspecie(virtual.getMascotaReal().getEspecie());
        virtual.setNivel(NIVEL_INICIAL);
        virtual.setExperiencia(EXPERIENCIA_INICIAL);
        virtual.setHambre(VALOR_MAX);
        virtual.setFelicidad(VALOR_MAX);
        virtual.setEnergia(VALOR_MAX);
        virtual.setHigiene(VALOR_MAX);
        virtual.setUltimaInteraccion(LocalDateTime.now());
        return toDTO(mascotaVirtualRepository.save(virtual));
    }

    private MascotaVirtual nuevaParaMascota(Integer idMascota) {
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con id: " + idMascota));
        MascotaVirtual virtual = new MascotaVirtual();
        virtual.setMascotaReal(mascota);
        return virtual;
    }

    private MascotaVirtualDTO toDTO(MascotaVirtual v) {
        return MascotaVirtualDTO.builder()
                .idVirtual(v.getIdVirtual())
                .idUsuario(v.getUsuario() != null ? v.getUsuario().getIdUsuario() : null)
                .idMascotaReal(v.getMascotaReal() != null ? v.getMascotaReal().getIdMascota() : null)
                .nombre(v.getNombre())
                .especie(v.getEspecie())
                .nivel(v.getNivel())
                .experiencia(v.getExperiencia())
                .hambre(v.getHambre())
                .felicidad(v.getFelicidad())
                .energia(v.getEnergia())
                .higiene(v.getHigiene())
                .ultimaInteraccion(v.getUltimaInteraccion())
                .build();
    }
}
