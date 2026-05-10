package com.pawlink.api.service;

import com.pawlink.api.dto.AccionRequestDTO;
import com.pawlink.api.dto.AccionResponseDTO;
import com.pawlink.api.dto.MascotaVirtualDTO;
import com.pawlink.api.model.MascotaVirtual;
import com.pawlink.api.repository.MascotaVirtualRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccionService {

    private static final int PUNTOS_POR_ACCION = 10;
    private static final int EFECTO_ACCION     = 40;
    private static final int STAT_MAX          = 100;
    private static final int XP_POR_NIVEL      = 200;

    private final MascotaVirtualRepository mascotaVirtualRepository;

    @Transactional
    public AccionResponseDTO realizar(AccionRequestDTO request) {
        MascotaVirtual mascota = mascotaVirtualRepository.findById(request.getIdMascotaVirtual())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mascota virtual no encontrada con id: " + request.getIdMascotaVirtual()));

        aplicarEfecto(mascota, request.getTipo());
        mascota.setUltimaInteraccion(LocalDateTime.now());
        MascotaVirtual saved = mascotaVirtualRepository.save(mascota);

        return AccionResponseDTO.builder()
                .idAccion(0)
                .puntosGanados(PUNTOS_POR_ACCION)
                .mascota(toDTO(saved))
                .build();
    }

    private void aplicarEfecto(MascotaVirtual m, String tipo) {
        int hambre    = m.getHambre()    != null ? m.getHambre()    : 100;
        int felicidad = m.getFelicidad() != null ? m.getFelicidad() : 100;
        int higiene   = m.getHigiene()   != null ? m.getHigiene()   : 100;

        switch (tipo) {
            case "alimentar" -> m.setHambre(Math.min(STAT_MAX, hambre    + EFECTO_ACCION));
            case "jugar"     -> m.setFelicidad(Math.min(STAT_MAX, felicidad + EFECTO_ACCION));
            case "bañar"     -> m.setHigiene(Math.min(STAT_MAX, higiene   + EFECTO_ACCION));
            default -> throw new IllegalArgumentException("Tipo de acción desconocido: " + tipo);
        }

        int exp   = m.getExperiencia() != null ? m.getExperiencia() : 0;
        int nivel = m.getNivel()       != null ? m.getNivel()       : 1;
        int nuevaExp = exp + PUNTOS_POR_ACCION;
        if (nuevaExp >= XP_POR_NIVEL) {
            m.setNivel(nivel + 1);
            nuevaExp -= XP_POR_NIVEL;
        }
        m.setExperiencia(nuevaExp);
    }

    private MascotaVirtualDTO toDTO(MascotaVirtual v) {
        return MascotaVirtualDTO.builder()
                .idVirtual(v.getIdVirtual())
                .idUsuario(v.getUsuario()     != null ? v.getUsuario().getIdUsuario()         : null)
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
