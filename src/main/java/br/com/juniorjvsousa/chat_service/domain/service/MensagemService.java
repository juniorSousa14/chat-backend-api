package br.com.juniorjvsousa.chat_service.domain.service;

import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.MensagemRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MensagemService {

    private final UsuarioRepository usuarioRepository;
    private final MensagemRepository mensagemRepository;

    @Transactional
    public Mensagem enviarMensagem(UUID usuarioId, String conteudo) {
        Usuario remetente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário remetente não encontrado"));

        Mensagem mensagem = Mensagem.builder()
                .conteudo(conteudo)
                .dataEnvio(LocalDateTime.now())
                .remetente(remetente)
                .build();

        return mensagemRepository.save(mensagem);
    }

    public List<Mensagem> listarTodasMensagens() {
        return mensagemRepository.findAll();
    }
}
