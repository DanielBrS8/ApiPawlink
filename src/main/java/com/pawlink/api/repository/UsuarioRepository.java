package com.pawlink.api.repository;

import com.pawlink.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    List<Usuario> findByCentro_IdCentro(Integer idCentro);
}
