package com.pawlink.api.repository;

import com.pawlink.api.model.CentroVeterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CentroVeterinarioRepository extends JpaRepository<CentroVeterinario, Integer> {

    List<CentroVeterinario> findByCiudadIgnoreCase(String ciudad);
}
