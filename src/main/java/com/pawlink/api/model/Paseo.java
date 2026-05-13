package com.pawlink.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paseos")
@Getter
@Setter
@NoArgsConstructor
public class Paseo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paseo")
    private Integer idPaseo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mascota", referencedColumnName = "id_mascota", nullable = false)
    private Mascota mascota;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "duracion", length = 20)
    private String duracion;

    @Column(name = "distancia", precision = 6, scale = 2)
    private BigDecimal distancia;

    @Column(name = "ruta", columnDefinition = "TEXT")
    private String ruta;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
