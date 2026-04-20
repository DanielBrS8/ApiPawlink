package com.pawlink.api.service;

import com.pawlink.api.dto.AlquilerDTO;
import com.pawlink.api.dto.AlquilerRequestDTO;
import com.pawlink.api.model.Alquiler;
import com.pawlink.api.model.Mascota;
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.AlquilerRepository;
import com.pawlink.api.repository.MascotaRepository;
import com.pawlink.api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<AlquilerDTO> findAll() {
        return alquilerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public AlquilerDTO findById(Integer id) {
        return alquilerRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con id: " + id));
    }

    public List<AlquilerDTO> findByCentro(Integer idCentro) {
        return alquilerRepository.findByMascota_Centro_IdCentro(idCentro)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AlquilerDTO> findByEstado(String estado) {
        return alquilerRepository.findByEstado(estado)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AlquilerDTO> findByMascota(Integer idMascota) {
        return alquilerRepository.findByMascota_IdMascota(idMascota)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AlquilerDTO> findByVoluntario(Integer idUsuario) {
        return alquilerRepository.findByVoluntario_IdUsuario(idUsuario)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public AlquilerDTO create(AlquilerRequestDTO request) {
        validarFechas(request.getFechaInicio(), request.getFechaFin());
        validarMascotaDisponible(request.getIdMascota());
        Alquiler alquiler = fromRequest(new Alquiler(), request);
        return toDTO(alquilerRepository.save(alquiler));
    }

    @Transactional
    public AlquilerDTO cambiarEstado(Integer idAlquiler, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        Alquiler alquiler = alquilerRepository.findById(idAlquiler)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con id: " + idAlquiler));

        alquiler.setEstado(nuevoEstado);

        String estadoNormalizado = nuevoEstado.trim().toLowerCase();
        if (estadoNormalizado.equals("aprobado") || estadoNormalizado.equals("activa")) {
            Mascota mascota = alquiler.getMascota();
            if (mascota != null) {
                mascota.setDisponibleAlquiler(0);
                mascotaRepository.save(mascota);
            }
        }

        return toDTO(alquilerRepository.save(alquiler));
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaInicio es obligatoria");
        }
        if (fin != null && fin.isBefore(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "fechaFin debe ser mayor o igual que fechaInicio");
        }
    }

    private void validarMascotaDisponible(Integer idMascota) {
        if (idMascota == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idMascota es obligatorio");
        }
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con id: " + idMascota));
        if (mascota.getDisponibleAlquiler() == null || mascota.getDisponibleAlquiler() != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La mascota ya no está disponible para alquiler");
        }
    }

    @Transactional
    public AlquilerDTO update(Integer id, AlquilerRequestDTO request) {
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con id: " + id));
        fromRequest(alquiler, request);
        return toDTO(alquilerRepository.save(alquiler));
    }

    @Transactional
    public void delete(Integer id) {
        if (!alquilerRepository.existsById(id)) {
            throw new EntityNotFoundException("Alquiler no encontrado con id: " + id);
        }
        alquilerRepository.deleteById(id);
    }

    // ---------------------------------------------------------------------------
    // Mappers internos
    // ---------------------------------------------------------------------------

    private AlquilerDTO toDTO(Alquiler a) {
        return AlquilerDTO.builder()
                .idAlquiler(a.getIdAlquiler())
                .idMascota(a.getMascota() != null ? a.getMascota().getIdMascota() : null)
                .nombreMascota(a.getMascota() != null ? a.getMascota().getNombre() : null)
                .especieMascota(a.getMascota() != null ? a.getMascota().getEspecie() : null)
                .idVoluntario(a.getVoluntario() != null ? a.getVoluntario().getIdUsuario() : null)
                .nombreVoluntario(a.getVoluntario() != null ? a.getVoluntario().getNombre() : null)
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .build();
    }

    private Alquiler fromRequest(Alquiler alquiler, AlquilerRequestDTO req) {
        Mascota mascota = mascotaRepository.findById(req.getIdMascota())
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con id: " + req.getIdMascota()));

        Usuario voluntario = null;
        if (req.getIdVoluntario() != null) {
            voluntario = usuarioRepository.findById(req.getIdVoluntario())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + req.getIdVoluntario()));
        }

        alquiler.setMascota(mascota);
        alquiler.setVoluntario(voluntario);
        alquiler.setFechaInicio(req.getFechaInicio());
        alquiler.setFechaFin(req.getFechaFin());
        alquiler.setEstado(req.getEstado() != null ? req.getEstado() : "pendiente");
        return alquiler;
    }
}
