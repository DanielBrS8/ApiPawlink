package com.pawlink.api.repository;

import com.pawlink.api.model.Paseo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaseoRepository extends JpaRepository<Paseo, Integer> {

    List<Paseo> findByMascota_IdMascotaOrderByFechaDesc(Integer idMascota);

    List<Paseo> findAllByOrderByFechaDesc();
}
