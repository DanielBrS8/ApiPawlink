package com.pawlink.api.repository;

import com.pawlink.api.model.HiloForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HiloForoRepository extends JpaRepository<HiloForo, Long> {

    List<HiloForo> findAllByOrderByFechaDesc();
}
