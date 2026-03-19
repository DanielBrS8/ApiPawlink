package com.pawlink.api.service;

import com.pawlink.api.dto.MascotaDTO;
import com.pawlink.api.dto.MascotaRequestDTO;
import com.pawlink.api.model.CentroVeterinario;
import com.pawlink.api.model.Mascota;
import com.pawlink.api.repository.CentroVeterinarioRepository;
import com.pawlink.api.repository.MascotaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final CentroVeterinarioRepository centroRepository;

    public List<MascotaDTO> findAll() {
        return mascotaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public MascotaDTO findById(Integer id) {
        return mascotaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con id: " + id));
    }

    public List<MascotaDTO> findDisponibles() {
        return mascotaRepository.findByDisponibleAlquiler(1)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public MascotaDTO create(MascotaRequestDTO request) {
        Mascota mascota = fromRequest(new Mascota(), request);
        return toDTO(mascotaRepository.save(mascota));
    }

    @Transactional
    public MascotaDTO update(Integer id, MascotaRequestDTO request) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con id: " + id));
        fromRequest(mascota, request);
        return toDTO(mascotaRepository.save(mascota));
    }

    @Transactional
    public void delete(Integer id) {
        if (!mascotaRepository.existsById(id)) {
            throw new EntityNotFoundException("Mascota no encontrada con id: " + id);
        }
        mascotaRepository.deleteById(id);
    }

    // ---------------------------------------------------------------------------
    // Mappers internos — sin librería externa para mantener el MVP simple
    // ---------------------------------------------------------------------------

    private MascotaDTO toDTO(Mascota m) {
        return MascotaDTO.builder()
                .idMascota(m.getIdMascota())
                .idCentro(m.getCentro() != null ? m.getCentro().getIdCentro() : null)
                .nombreCentro(m.getCentro() != null ? m.getCentro().getNombre() : null)
                .nombre(m.getNombre())
                .especie(m.getEspecie())
                .raza(m.getRaza())
                .fechaNacimiento(m.getFechaNacimiento())
                .peso(m.getPeso())
                .estadoSalud(m.getEstadoSalud())
                .disponibleAlquiler(m.getDisponibleAlquiler())
                .foto(m.getFoto())
                .notas(m.getNotas())
                .fechaCreacion(m.getFechaCreacion())
                .build();
    }

    private Mascota fromRequest(Mascota mascota, MascotaRequestDTO req) {
        if (req.getIdCentro() != null) {
            CentroVeterinario centro = centroRepository.findById(req.getIdCentro())
                    .orElseThrow(() -> new EntityNotFoundException("Centro no encontrado con id: " + req.getIdCentro()));
            mascota.setCentro(centro);
        } else {
            mascota.setCentro(null);
        }
        mascota.setNombre(req.getNombre());
        mascota.setEspecie(req.getEspecie());
        mascota.setRaza(req.getRaza());
        mascota.setFechaNacimiento(req.getFechaNacimiento());
        mascota.setPeso(req.getPeso());
        mascota.setEstadoSalud(req.getEstadoSalud());
        mascota.setDisponibleAlquiler(req.getDisponibleAlquiler() != null ? req.getDisponibleAlquiler() : 1);
        mascota.setFoto(req.getFoto());
        mascota.setNotas(req.getNotas());
        return mascota;
    }
}
