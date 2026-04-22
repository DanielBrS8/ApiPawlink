package com.pawlink.api.repository;

import com.pawlink.api.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {

    List<Mensaje> findByConsultaChat_IdConversacionOrderByFechaAsc(Integer idConversacion);
}
