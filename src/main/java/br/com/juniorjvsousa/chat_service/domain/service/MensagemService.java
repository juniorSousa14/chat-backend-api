package br.com.juniorjvsousa.chat_service.domain.service;

import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.GrupoRepository;
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
    private final GrupoRepository grupoRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public Mensagem enviarMensagem(UUID usuarioId, String conteudo, UUID grupoId, UUID destinatarioId) {
        Usuario remetente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário remetente não encontrado"));

        Mensagem.MensagemBuilder mensagemBuilder = Mensagem.builder()
                .conteudo(conteudo)
                .remetente(remetente)
                .dataEnvio(LocalDateTime.now());

        if (grupoId != null && destinatarioId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma mensagem não pode ser enviada para um grupo e um usuário ao mesmo tempo.");
        }

        if (grupoId != null) {
            Grupo grupo = grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));

            if (!usuarioService.usuarioPertenceAoGrupo(usuarioId, grupoId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não pertence ao grupo especificado.");
            }
            mensagemBuilder.grupo(grupo);
        } else if (destinatarioId != null) {
            Usuario destinatario = usuarioRepository.findById(destinatarioId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário destinatário não encontrado"));
            mensagemBuilder.usuarioDestino(destinatario);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deve ser especificado um grupo ou um usuário destinatário.");
        }

        return mensagemRepository.save(mensagemBuilder.build());
    }

    public List<Mensagem> listarMensagensDoGrupo(UUID grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));

        return mensagemRepository.findByGrupoOrderByDataEnvioAsc(grupo);
    }

    public List<Mensagem> listarMensagensPorUsuario(UUID usuario1Id, UUID usuario2Id) {
        Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário 1 não encontrado"));

        Usuario usuario2 = usuarioRepository.findById(usuario2Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário 2 não encontrado"));

        return mensagemRepository.findConversaEntre(usuario1, usuario2);
    }
}