package com.pawlink.api.repository;

import com.pawlink.api.model.ResenaCentro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResenaCentroRepository extends JpaRepository<ResenaCentro, Long> {
}
