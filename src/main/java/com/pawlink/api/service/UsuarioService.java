package com.pawlink.api.service;

import com.pawlink.api.dto.HistorialAdopcionesDTO;
import com.pawlink.api.dto.UsuarioDTO;
import com.pawlink.api.model.Alquiler;
import com.pawlink.api.model.CentroVeterinario;
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.AlquilerRepository;
import com.pawlink.api.repository.CentroVeterinarioRepository;
import com.pawlink.api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AlquilerRepository alquilerRepository;
    private final CentroVeterinarioRepository centroRepository;

    public HistorialAdopcionesDTO obtenerHistorialAdopciones(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Usuario no encontrado con id: " + idUsuario);
        }

        List<Alquiler> alquileres = alquilerRepository.findByVoluntario_IdUsuario(idUsuario);

        if (alquileres.isEmpty()) {
            return HistorialAdopcionesDTO.builder()
                    .totalMascotas(0)
                    .duracionMediaDias(0.0)
                    .build();
        }

        int totalMascotas = (int) alquileres.stream()
                .map(a -> a.getMascota() != null ? a.getMascota().getIdMascota() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        OptionalDouble media = alquileres.stream()
                .filter(a -> a.getFechaInicio() != null && a.getFechaFin() != null)
                .mapToLong(a -> ChronoUnit.DAYS.between(a.getFechaInicio(), a.getFechaFin()))
                .average();

        return HistorialAdopcionesDTO.builder()
                .totalMascotas(totalMascotas)
                .duracionMediaDias(media.orElse(0.0))
                .build();
    }

    @Transactional
    public UsuarioDTO vincularCentro(Integer idUsuario, Integer idCentro) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + idUsuario));

        CentroVeterinario centro = centroRepository.findById(idCentro)
                .orElseThrow(() -> new EntityNotFoundException("Centro no encontrado con id: " + idCentro));

        usuario.setCentro(centro);
        Usuario guardado = usuarioRepository.save(usuario);

        return toDTO(guardado);
    }

    private UsuarioDTO toDTO(Usuario u) {
        return UsuarioDTO.builder()
                .idUsuario(u.getIdUsuario())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .idCentro(u.getCentro() != null ? u.getCentro().getIdCentro() : null)
                .nombreCentro(u.getCentro() != null ? u.getCentro().getNombre() : null)
                .build();
    }
}
