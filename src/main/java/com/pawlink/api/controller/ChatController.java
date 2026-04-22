package com.pawlink.api.controller;

import com.pawlink.api.dto.ConsultaChatDTO;
import com.pawlink.api.dto.MensajeDTO;
import com.pawlink.api.dto.UsuarioDTO;
import com.pawlink.api.security.JwtUtil;
import com.pawlink.api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/usuario/{idUsuario}/conversaciones")
    public ResponseEntity<List<ConsultaChatDTO>> getConversaciones(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer idUsuario) {
        validarToken(authHeader);
        return ResponseEntity.ok(chatService.findConversacionesUsuario(idUsuario));
    }

    @GetMapping("/usuario/{idUsuario}/contactos")
    public ResponseEntity<List<UsuarioDTO>> getContactos(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer idUsuario) {
        validarToken(authHeader);
        return ResponseEntity.ok(chatService.findContactos(idUsuario));
    }

    @PostMapping("/conversacion")
    public ResponseEntity<ConsultaChatDTO> crearConversacion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        validarToken(authHeader);

        Integer idUsuario = body.get("idUsuario") != null
                ? Integer.valueOf(body.get("idUsuario").toString()) : null;
        Integer idVeterinario = body.get("idVeterinario") != null
                ? Integer.valueOf(body.get("idVeterinario").toString()) : null;
        String asunto = body.get("asunto") != null ? body.get("asunto").toString() : null;

        ConsultaChatDTO creada = chatService.crearConversacion(idUsuario, idVeterinario, asunto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/conversacion/{idConversacion}/mensajes")
    public ResponseEntity<List<MensajeDTO>> getMensajes(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer idConversacion) {
        validarToken(authHeader);
        return ResponseEntity.ok(chatService.findMensajes(idConversacion));
    }

    @PostMapping("/conversacion/{idConversacion}/mensaje")
    public ResponseEntity<MensajeDTO> enviarMensaje(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer idConversacion,
            @RequestBody Map<String, Object> body) {
        validarToken(authHeader);

        Integer idEmisor = body.get("idEmisor") != null
                ? Integer.valueOf(body.get("idEmisor").toString())
                : null;
        String contenido = body.get("contenido") != null ? body.get("contenido").toString() : null;

        MensajeDTO creado = chatService.enviarMensaje(idConversacion, idEmisor, contenido);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    private void validarToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token ausente");
        }
        try {
            JwtUtil.verificarToken(authHeader.substring(7));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }
}
