package com.pawlink.api.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.pawlink.api.dto.PaseoDTO;
import com.pawlink.api.dto.PaseoRequestDTO;
import com.pawlink.api.model.Mascota;
import com.pawlink.api.model.Paseo;
import com.pawlink.api.repository.MascotaRepository;
import com.pawlink.api.repository.PaseoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaseoService {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<PaseoDTO.Coordenada>> RUTA_TYPE = new TypeReference<>() {};

    private final PaseoRepository paseoRepository;
    private final MascotaRepository mascotaRepository;

    public List<PaseoDTO> findAll() {
        return paseoRepository.findAllByOrderByFechaDesc().stream().map(this::toDTO).toList();
    }

    public List<PaseoDTO> findByMascota(Integer idMascota) {
        if (!mascotaRepository.existsById(idMascota)) {
            throw new EntityNotFoundException("Mascota no encontrada con id: " + idMascota);
        }
        return paseoRepository.findByMascota_IdMascotaOrderByFechaDesc(idMascota)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public PaseoDTO create(PaseoRequestDTO request) {
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mascota no encontrada con id: " + request.getMascotaId()));

        Paseo paseo = new Paseo();
        paseo.setMascota(mascota);
        paseo.setFecha(request.getFecha());
        paseo.setDuracion(request.getDuracion());
        paseo.setDistancia(request.getDistancia());
        paseo.setRuta(serializarRuta(request.getRuta()));

        return toDTO(paseoRepository.save(paseo));
    }

    private PaseoDTO toDTO(Paseo p) {
        return PaseoDTO.builder()
                .id(p.getIdPaseo())
                .mascotaId(p.getMascota() != null ? p.getMascota().getIdMascota() : null)
                .fecha(p.getFecha())
                .duracion(p.getDuracion())
                .distancia(p.getDistancia())
                .ruta(deserializarRuta(p.getRuta()))
                .build();
    }

    private String serializarRuta(List<PaseoDTO.Coordenada> ruta) {
        if (ruta == null || ruta.isEmpty()) return "[]";
        try {
            return MAPPER.writeValueAsString(ruta);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<PaseoDTO.Coordenada> deserializarRuta(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, RUTA_TYPE);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
