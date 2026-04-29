package com.pawlink.api.service;

import com.pawlink.api.dto.VacunaDTO;
import com.pawlink.api.model.Mascota;
import com.pawlink.api.model.Vacuna;
import com.pawlink.api.repository.MascotaRepository;
import com.pawlink.api.repository.VacunaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VacunaService {

    private final VacunaRepository vacunaRepository;
    private final MascotaRepository mascotaRepository;

    public List<VacunaDTO> obtenerVacunasPorMascota(Integer idMascota) {
        if (!mascotaRepository.existsById(idMascota)) {
            throw new EntityNotFoundException("Mascota no encontrada con id: " + idMascota);
        }
        return vacunaRepository.findByMascota_IdMascota(idMascota)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public VacunaDTO registrarVacuna(VacunaDTO vacunaDto) {
        Mascota mascota = mascotaRepository.findById(vacunaDto.getIdMascota())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mascota no encontrada con id: " + vacunaDto.getIdMascota()));

        Vacuna vacuna = new Vacuna();
        vacuna.setMascota(mascota);
        vacuna.setNombreVacuna(vacunaDto.getNombreVacuna());
        vacuna.setFechaAdministracion(vacunaDto.getFechaAdministracion());
        vacuna.setFechaProximaDosis(vacunaDto.getFechaProximaDosis());
        vacuna.setVeterinario(vacunaDto.getVeterinario());
        vacuna.setNotas(vacunaDto.getNotas());

        return toDTO(vacunaRepository.save(vacuna));
    }

    private VacunaDTO toDTO(Vacuna v) {
        return VacunaDTO.builder()
                .idVacuna(v.getIdVacuna())
                .idMascota(v.getMascota().getIdMascota())
                .nombreVacuna(v.getNombreVacuna())
                .fechaAdministracion(v.getFechaAdministracion())
                .fechaProximaDosis(v.getFechaProximaDosis())
                .veterinario(v.getVeterinario())
                .notas(v.getNotas())
                .build();
    }
}
