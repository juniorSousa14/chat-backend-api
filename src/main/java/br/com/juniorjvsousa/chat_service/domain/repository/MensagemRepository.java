package br.com.juniorjvsousa.chat_service.domain.repository;

import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemRepository extends JpaRepository<Mensagem, UUID> {

}
