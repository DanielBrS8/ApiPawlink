package com.pawlink.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas_chat")
@Getter
@Setter
@NoArgsConstructor
public class ConsultaChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Integer idConversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mascota", referencedColumnName = "id_mascota")
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veterinario", referencedColumnName = "id_usuario")
    private Usuario veterinario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alquiler", referencedColumnName = "id_alquiler")
    private Alquiler alquiler;

    @Column(name = "asunto")
    private String asunto;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "estado", length = 50)
    private String estado = "activa";

    @PrePersist
    protected void onCreate() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
    }
}
