package com.pawlink.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Getter
@Setter
@NoArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", referencedColumnName = "id_conversacion")
    private ConsultaChat consultaChat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor", referencedColumnName = "id_usuario")
    private Usuario emisor;

    @Column(name = "contenido", columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "leido")
    private Boolean leido = false;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (leido == null) {
            leido = false;
        }
    }
}
