package com.pawlink.api.repository;

import com.pawlink.api.model.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Integer> {

    List<Alquiler> findByMascota_IdMascota(Integer idMascota);

    List<Alquiler> findByVoluntario_IdUsuario(Integer idUsuario);

    List<Alquiler> findByEstado(String estado);

    List<Alquiler> findByMascota_Centro_IdCentro(Integer idCentro);
}
