package com.pawlink.api.controller;

import com.pawlink.api.dto.CampanaWebDTO;
import com.pawlink.api.dto.CentroWebDTO;
import com.pawlink.api.dto.DonacionCreateDTO;
import com.pawlink.api.dto.HiloCreateDTO;
import com.pawlink.api.dto.HiloWebDTO;
import com.pawlink.api.dto.MascotaWebDTO;
import com.pawlink.api.dto.ResenaCreateDTO;
import com.pawlink.api.model.Campana;
import com.pawlink.api.model.CentroVeterinario;
import com.pawlink.api.model.Donacion;
import com.pawlink.api.model.HiloForo;
import com.pawlink.api.model.ResenaCentro;
import com.pawlink.api.repository.CampanaRepository;
import com.pawlink.api.repository.CentroVeterinarioRepository;
import com.pawlink.api.repository.DonacionRepository;
import com.pawlink.api.repository.HiloForoRepository;
import com.pawlink.api.repository.MascotaRepository;
import com.pawlink.api.repository.ResenaCentroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/web")
@RequiredArgsConstructor
public class WebPublicController {

    private final CentroVeterinarioRepository centroRepository;
    private final MascotaRepository mascotaRepository;
    private final CampanaRepository campanaRepository;
    private final HiloForoRepository hiloForoRepository;
    private final DonacionRepository donacionRepository;
    private final ResenaCentroRepository resenaCentroRepository;

    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping("/centros")
    public ResponseEntity<List<CentroWebDTO>> getCentros() {
        List<CentroWebDTO> centros = centroRepository.findAll().stream()
                .map(c -> CentroWebDTO.builder()
                        .idCentro(c.getIdCentro())
                        .nombre(c.getNombre())
                        .ciudad(c.getCiudad())
                        .especialidad(c.getEspecialidad())
                        .fotoUrl(c.getFoto())
                        .build())
                .toList();
        return ResponseEntity.ok(centros);
    }

    @GetMapping("/adopciones")
    public ResponseEntity<List<MascotaWebDTO>> getAdopciones() {
        List<MascotaWebDTO> mascotas = mascotaRepository.findByDisponibleAlquiler(1).stream()
                .map(m -> MascotaWebDTO.builder()
                        .idMascota(m.getIdMascota().longValue())
                        .nombre(m.getNombre())
                        .especie(m.getEspecie())
                        .raza(m.getRaza())
                        .edadAprox(calcularEdad(m.getFechaNacimiento()))
                        .fotoUrl(m.getFoto())
                        .build())
                .toList();
        return ResponseEntity.ok(mascotas);
    }

    @Transactional(readOnly = true)
    @GetMapping("/campanas")
    public ResponseEntity<List<CampanaWebDTO>> getCampanas() {
        List<CampanaWebDTO> campanas = campanaRepository.findByEstado("abierta").stream()
                .map(c -> CampanaWebDTO.builder()
                        .idCampana(c.getIdCampana())
                        .titulo(c.getTitulo())
                        .objetivoDinero(c.getObjetivoDinero())
                        .recaudado(BigDecimal.ZERO)
                        .fotoUrl(c.getMascota() != null ? c.getMascota().getFoto() : null)
                        .build())
                .toList();
        return ResponseEntity.ok(campanas);
    }

    @Transactional(readOnly = true)
    @GetMapping("/foro/hilos")
    public ResponseEntity<List<HiloWebDTO>> getHilos() {
        List<HiloWebDTO> hilos = hiloForoRepository.findAllByOrderByFechaDesc().stream()
                .map(h -> HiloWebDTO.builder()
                        .idHilo(h.getIdHilo())
                        .titulo(h.getTitulo())
                        .autor(h.getAutor() != null ? h.getAutor().getNombre() : "Anonimo")
                        .respuestas(0)
                        .fechaFormateada(h.getFecha() != null
                                ? h.getFecha().format(FECHA_FORMAT)
                                : "")
                        .build())
                .toList();
        return ResponseEntity.ok(hilos);
    }

    @PostMapping("/foro/hilos")
    public ResponseEntity<HiloWebDTO> crearHilo(@RequestBody HiloCreateDTO dto) {
        HiloForo hilo = new HiloForo();
        hilo.setTitulo(dto.getTitulo());
        hilo.setContenido(dto.getContenido());
        hilo.setFecha(LocalDateTime.now());
        hilo.setAutor(null);
        HiloForo guardado = hiloForoRepository.save(hilo);

        HiloWebDTO respuesta = HiloWebDTO.builder()
                .idHilo(guardado.getIdHilo())
                .titulo(guardado.getTitulo())
                .autor("Anonimo")
                .respuestas(0)
                .fechaFormateada(guardado.getFecha().format(FECHA_FORMAT))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/campanas/{idCampana}/donar")
    public ResponseEntity<Void> donarCampana(@PathVariable Long idCampana,
                                             @RequestBody DonacionCreateDTO dto) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaña no encontrada"));

        Donacion donacion = new Donacion();
        donacion.setCampana(campana);
        donacion.setUsuario(null);
        donacion.setCantidad(dto.getCantidad());
        donacion.setFecha(LocalDateTime.now());
        donacionRepository.save(donacion);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/centros/{idCentro}/resenas")
    public ResponseEntity<Void> crearResena(@PathVariable Integer idCentro,
                                            @RequestBody ResenaCreateDTO dto) {
        CentroVeterinario centro = centroRepository.findById(idCentro)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro no encontrado"));

        ResenaCentro resena = new ResenaCentro();
        resena.setCentro(centro);
        resena.setUsuario(null);
        resena.setPuntuacion(dto.getPuntuacion());
        resena.setComentario(dto.getComentario());
        resena.setFecha(LocalDateTime.now());
        resenaCentroRepository.save(resena);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private String calcularEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return "Desconocida";
        }
        Period periodo = Period.between(fechaNacimiento, LocalDate.now());
        if (periodo.getYears() >= 1) {
            return periodo.getYears() + (periodo.getYears() == 1 ? " año" : " años");
        }
        int meses = periodo.getMonths();
        return meses <= 1 ? "1 mes" : meses + " meses";
    }
}
