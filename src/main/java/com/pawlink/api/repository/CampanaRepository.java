package com.pawlink.api.repository;

import com.pawlink.api.model.Campana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampanaRepository extends JpaRepository<Campana, Long> {

    List<Campana> findByEstado(String estado);
}
