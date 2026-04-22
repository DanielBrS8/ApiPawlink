package com.pawlink.api.repository;

import com.pawlink.api.model.ConsultaChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaChatRepository extends JpaRepository<ConsultaChat, Integer> {

    List<ConsultaChat> findByUsuario_IdUsuario(Integer id);

    List<ConsultaChat> findByVeterinario_IdUsuario(Integer id);
}
