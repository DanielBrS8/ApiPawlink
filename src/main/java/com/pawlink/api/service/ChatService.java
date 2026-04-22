package com.pawlink.api.service;

import com.pawlink.api.chat.ServidorChat;
import com.pawlink.api.dto.ConsultaChatDTO;
import com.pawlink.api.dto.MensajeDTO;
import com.pawlink.api.dto.UsuarioDTO;
import com.pawlink.api.model.ConsultaChat;
import com.pawlink.api.model.Mensaje;
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.ConsultaChatRepository;
import com.pawlink.api.repository.MensajeRepository;
import com.pawlink.api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado con id: ";

    private final ConsultaChatRepository consultaChatRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServidorChat servidorChat;

    public List<ConsultaChatDTO> findConversacionesUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idUsuario es obligatorio");
        }

        Map<Integer, ConsultaChat> unicas = new LinkedHashMap<>();
        for (ConsultaChat c : consultaChatRepository.findByUsuario_IdUsuario(idUsuario)) {
            unicas.putIfAbsent(c.getIdConversacion(), c);
        }
        for (ConsultaChat c : consultaChatRepository.findByVeterinario_IdUsuario(idUsuario)) {
            unicas.putIfAbsent(c.getIdConversacion(), c);
        }

        List<ConsultaChatDTO> result = new ArrayList<>(unicas.size());
        for (ConsultaChat c : unicas.values()) {
            result.add(toDTO(c, idUsuario));
        }
        result.sort(Comparator.comparing(
                ConsultaChatDTO::getFechaUltimoMensaje,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    public List<UsuarioDTO> findContactos(Integer idUsuario) {
        Usuario solicitante = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException(
                        USUARIO_NO_ENCONTRADO + idUsuario));

        List<Usuario> candidatos = "user".equalsIgnoreCase(solicitante.getRol())
                ? usuarioRepository.findByRolNotAndActivo("user", 1)
                : usuarioRepository.findByRolAndActivo("user", 1);

        List<UsuarioDTO> result = new ArrayList<>(candidatos.size());
        for (Usuario u : candidatos) {
            if (u.getIdUsuario().equals(idUsuario)) continue;
            result.add(UsuarioDTO.builder()
                    .idUsuario(u.getIdUsuario())
                    .nombre(u.getNombre())
                    .email(u.getEmail())
                    .rol(u.getRol())
                    .idCentro(u.getCentro() != null ? u.getCentro().getIdCentro() : null)
                    .nombreCentro(u.getCentro() != null ? u.getCentro().getNombre() : null)
                    .build());
        }
        return result;
    }

    @Transactional
    public ConsultaChatDTO crearConversacion(Integer idUsuario, Integer idVeterinario, String asunto) {
        if (idUsuario == null || idVeterinario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario e idVeterinario son obligatorios");
        }
        if (idUsuario.equals(idVeterinario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los participantes deben ser distintos");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException(
                        USUARIO_NO_ENCONTRADO + idUsuario));
        Usuario veterinario = usuarioRepository.findById(idVeterinario)
                .orElseThrow(() -> new EntityNotFoundException(
                        USUARIO_NO_ENCONTRADO + idVeterinario));

        ConsultaChat conversacion = new ConsultaChat();
        conversacion.setUsuario(usuario);
        conversacion.setVeterinario(veterinario);
        conversacion.setAsunto(asunto);
        conversacion.setEstado("activa");
        ConsultaChat guardada = consultaChatRepository.save(conversacion);

        return toDTO(guardada, idVeterinario);
    }

    public List<MensajeDTO> findMensajes(Integer idConversacion) {
        if (!consultaChatRepository.existsById(idConversacion)) {
            throw new EntityNotFoundException("Conversación no encontrada con id: " + idConversacion);
        }
        return mensajeRepository
                .findByConsultaChat_IdConversacionOrderByFechaAsc(idConversacion)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public MensajeDTO enviarMensaje(Integer idConversacion, Integer idEmisor, String contenido) {
        if (idEmisor == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idEmisor es obligatorio");
        }
        if (contenido == null || contenido.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contenido es obligatorio");
        }

        ConsultaChat conversacion = consultaChatRepository.findById(idConversacion)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Conversación no encontrada con id: " + idConversacion));

        Integer idCliente = conversacion.getUsuario() != null
                ? conversacion.getUsuario().getIdUsuario() : null;
        Integer idVeterinario = conversacion.getVeterinario() != null
                ? conversacion.getVeterinario().getIdUsuario() : null;

        if (!idEmisor.equals(idCliente) && !idEmisor.equals(idVeterinario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El emisor no pertenece a esta conversación");
        }

        Usuario emisor = usuarioRepository.findById(idEmisor)
                .orElseThrow(() -> new EntityNotFoundException(
                        USUARIO_NO_ENCONTRADO + idEmisor));

        Mensaje mensaje = new Mensaje();
        mensaje.setConsultaChat(conversacion);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(contenido);
        mensaje.setLeido(false);
        Mensaje guardado = mensajeRepository.save(mensaje);

        Integer idDestinatario = idEmisor.equals(idCliente) ? idVeterinario : idCliente;
        if (idDestinatario != null) {
            servidorChat.notificar(idDestinatario, "NOTIFY:NUEVO_MENSAJE:" + idConversacion);
        }

        return toDTO(guardado);
    }

    // ---------------------------------------------------------------------------
    // Mappers internos
    // ---------------------------------------------------------------------------

    private ConsultaChatDTO toDTO(ConsultaChat c, Integer idUsuarioActual) {
        Usuario cliente = c.getUsuario();
        Usuario vet = c.getVeterinario();

        Usuario otro = null;
        if (cliente != null && cliente.getIdUsuario().equals(idUsuarioActual)) {
            otro = vet;
        } else if (vet != null && vet.getIdUsuario().equals(idUsuarioActual)) {
            otro = cliente;
        }

        List<Mensaje> mensajes = mensajeRepository
                .findByConsultaChat_IdConversacionOrderByFechaAsc(c.getIdConversacion());
        Mensaje ultimo = mensajes.isEmpty() ? null : mensajes.get(mensajes.size() - 1);

        return ConsultaChatDTO.builder()
                .idConversacion(c.getIdConversacion())
                .asunto(c.getAsunto())
                .estado(c.getEstado())
                .fechaInicio(c.getFechaInicio())
                .idOtroParticipante(otro != null ? otro.getIdUsuario() : null)
                .nombreOtroParticipante(otro != null ? otro.getNombre() : null)
                .ultimoMensaje(ultimo != null ? ultimo.getContenido() : null)
                .fechaUltimoMensaje(ultimo != null ? ultimo.getFecha() : null)
                .build();
    }

    private MensajeDTO toDTO(Mensaje m) {
        return MensajeDTO.builder()
                .idMensaje(m.getIdMensaje())
                .idEmisor(m.getEmisor() != null ? m.getEmisor().getIdUsuario() : null)
                .contenido(m.getContenido())
                .fecha(m.getFecha())
                .leido(m.getLeido())
                .build();
    }
}
