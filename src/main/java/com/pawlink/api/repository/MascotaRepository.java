package com.pawlink.api.repository;

import com.pawlink.api.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    List<Mascota> findByEspecieIgnoreCase(String especie);

    List<Mascota> findByDisponibleAlquiler(Integer disponibleAlquiler);

    List<Mascota> findByCentro_IdCentro(Integer idCentro);
}
