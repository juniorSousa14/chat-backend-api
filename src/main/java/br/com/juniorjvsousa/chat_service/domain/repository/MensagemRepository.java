package br.com.juniorjvsousa.chat_service.domain.repository;

import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MensagemRepository extends JpaRepository<Mensagem, UUID> {

    List<Mensagem> findAllByGrupo(Grupo grupo);

    @Query("SELECT m FROM Mensagem m WHERE " +
            "(m.remetente = :usuario1 AND m.usuarioDestino = :usuario2) OR " +
            "(m.remetente = :usuario2 AND m.usuarioDestino = :usuario1) " +
            "ORDER BY m.dataEnvio ASC")
    List<Mensagem> findConversaEntre(@Param("usuario1") Usuario usuario1, @Param("usuario2") Usuario usuario2);

}
