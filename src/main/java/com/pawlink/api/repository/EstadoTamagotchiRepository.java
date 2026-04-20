package com.pawlink.api.repository;

import com.pawlink.api.model.EstadoTamagotchi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoTamagotchiRepository extends JpaRepository<EstadoTamagotchi, Long> {

    Optional<EstadoTamagotchi> findByMascota_IdMascota(Integer idMascota);
}
