package com.pawlink.api.repository;

import com.pawlink.api.model.MascotaVirtual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MascotaVirtualRepository extends JpaRepository<MascotaVirtual, Integer> {

    Optional<MascotaVirtual> findByMascotaReal_IdMascota(Integer idMascota);
}
