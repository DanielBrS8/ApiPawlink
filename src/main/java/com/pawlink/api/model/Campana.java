package com.pawlink.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "campañas_donacion")
@Getter
@Setter
@NoArgsConstructor
public class Campana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campaña")
    private Long idCampana;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "objetivo_dinero", precision = 10, scale = 2)
    private BigDecimal objetivoDinero;

    @Column(name = "estado", length = 50)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mascota", referencedColumnName = "id_mascota")
    private Mascota mascota;
}
