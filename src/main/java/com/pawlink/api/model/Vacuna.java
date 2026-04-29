package com.pawlink.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "vacunas")
@Getter
@Setter
@NoArgsConstructor
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vacuna")
    private Integer idVacuna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mascota", referencedColumnName = "id_mascota", nullable = false)
    private Mascota mascota;

    @Column(name = "nombre_vacuna", nullable = false, length = 150)
    private String nombreVacuna;

    @Column(name = "fecha_administracion", nullable = false)
    private LocalDate fechaAdministracion;

    @Column(name = "fecha_proxima_dosis")
    private LocalDate fechaProximaDosis;

    @Column(name = "veterinario", length = 150)
    private String veterinario;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;
}
