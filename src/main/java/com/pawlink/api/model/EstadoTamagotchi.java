package com.pawlink.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "estado_tamagotchi")
@Getter
@Setter
@NoArgsConstructor
public class EstadoTamagotchi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mascota", referencedColumnName = "id_mascota", unique = true, nullable = false)
    private Mascota mascota;

    @Column(name = "hambre", nullable = false)
    private double hambre;

    @Column(name = "energia", nullable = false)
    private double energia;

    @Column(name = "felicidad", nullable = false)
    private double felicidad;

    @Column(name = "suciedad", nullable = false)
    private double suciedad;

    @Column(name = "ultima_interaccion", nullable = false)
    private LocalDateTime ultimaInteraccion;
}
