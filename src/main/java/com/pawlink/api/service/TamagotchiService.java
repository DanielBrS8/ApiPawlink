package com.pawlink.api.service;

import com.pawlink.api.dto.TamagotchiDTO;
import com.pawlink.api.model.EstadoTamagotchi;
import com.pawlink.api.model.Mascota;
import com.pawlink.api.repository.EstadoTamagotchiRepository;
import com.pawlink.api.repository.MascotaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TamagotchiService {

    private static final double VALOR_INICIAL = 100.0;

    private final EstadoTamagotchiRepository estadoRepository;
    private final MascotaRepository mascotaRepository;

    @Transactional
    public TamagotchiDTO obtenerOCrear(Integer idMascota) {
        return estadoRepository.findByMascota_IdMascota(idMascota)
                .map(this::toDTO)
                .orElseGet(() -> crearEstadoPorDefecto(idMascota));
    }

    @Transactional
    public TamagotchiDTO actualizar(Integer idMascota, TamagotchiDTO request) {
        EstadoTamagotchi estado = estadoRepository.findByMascota_IdMascota(idMascota)
                .orElseGet(() -> nuevoEstadoParaMascota(idMascota));

        estado.setHambre(request.getHambre());
        estado.setEnergia(request.getEnergia());
        estado.setFelicidad(request.getFelicidad());
        estado.setSuciedad(request.getSuciedad());
        estado.setUltimaInteraccion(LocalDateTime.now());

        return toDTO(estadoRepository.save(estado));
    }

    private TamagotchiDTO crearEstadoPorDefecto(Integer idMascota) {
        EstadoTamagotchi estado = nuevoEstadoParaMascota(idMascota);
        estado.setHambre(VALOR_INICIAL);
        estado.setEnergia(VALOR_INICIAL);
        estado.setFelicidad(VALOR_INICIAL);
        estado.setSuciedad(VALOR_INICIAL);
        estado.setUltimaInteraccion(LocalDateTime.now());
        return toDTO(estadoRepository.save(estado));
    }

    private EstadoTamagotchi nuevoEstadoParaMascota(Integer idMascota) {
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con id: " + idMascota));
        EstadoTamagotchi estado = new EstadoTamagotchi();
        estado.setMascota(mascota);
        return estado;
    }

    private TamagotchiDTO toDTO(EstadoTamagotchi e) {
        return TamagotchiDTO.builder()
                .id(e.getId())
                .idMascota(e.getMascota() != null ? e.getMascota().getIdMascota() : null)
                .hambre(e.getHambre())
                .energia(e.getEnergia())
                .felicidad(e.getFelicidad())
                .suciedad(e.getSuciedad())
                .ultimaInteraccion(e.getUltimaInteraccion())
                .build();
    }
}
