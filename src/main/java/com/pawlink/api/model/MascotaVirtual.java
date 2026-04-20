package com.pawlink.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas_virtuales")
@Getter
@Setter
@NoArgsConstructor
public class MascotaVirtual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_virtual")
    private Integer idVirtual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mascota_real", referencedColumnName = "id_mascota")
    private Mascota mascotaReal;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "especie", length = 50)
    private String especie;

    @Column(name = "nivel")
    private Integer nivel;

    @Column(name = "experiencia")
    private Integer experiencia;

    @Column(name = "hambre")
    private Integer hambre;

    @Column(name = "felicidad")
    private Integer felicidad;

    @Column(name = "energia")
    private Integer energia;

    @Column(name = "higiene")
    private Integer higiene;

    @Column(name = "ultima_interaccion")
    private LocalDateTime ultimaInteraccion;
}
