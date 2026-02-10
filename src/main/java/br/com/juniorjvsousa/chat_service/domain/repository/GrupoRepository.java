package br.com.juniorjvsousa.chat_service.domain.repository;

import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GrupoRepository extends JpaRepository<Grupo, UUID> {

    boolean existsByIdAndAdministradores_Id(UUID grupoId, UUID usuarioId);
}
