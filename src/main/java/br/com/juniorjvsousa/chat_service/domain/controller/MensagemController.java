package br.com.juniorjvsousa.chat_service.domain.controller;

import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.service.MensagemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mensagens")
@RequiredArgsConstructor
public class MensagemController {

    private final MensagemService mensagemService;

    @PostMapping
    public ResponseEntity<Mensagem> enviarMensagem(@RequestBody NovaMensagemRequest request) {
        Mensagem mensagem = mensagemService.enviarMensagem(
                request.usuarioId(),
                request.conteudo(),
                request.grupoId(),
                request.usuario2Id()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
    }

    @GetMapping("/privada")
    public ResponseEntity<List<Mensagem>> listarMensagensEntreUsuarioEUsusario2(
            @RequestParam UUID usuarioId,
            @RequestParam UUID usuario2Id) {

        List<Mensagem> mensagens = mensagemService.listarMensagensPorUsuario(usuarioId, usuario2Id);
        return ResponseEntity.ok(mensagens);
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<MensagemResponse>> listarMensagensPorGrupo(@PathVariable UUID grupoId) {
        List<Mensagem> mensagens = mensagemService.listarMensagensDoGrupo(grupoId);

        List<MensagemResponse> resposta = mensagens.stream()
                .map(m -> new MensagemResponse(
                        m.getId(),
                        m.getConteudo(),
                        m.getDataEnvio(),
                        m.getRemetente().getNome(),
                        m.getRemetente().getId(),
                        true // É grupo
                ))
                .toList();

        return ResponseEntity.ok(resposta);
    }

    public record NovaMensagemRequest(UUID usuarioId, UUID usuario2Id, String conteudo, UUID grupoId) {
    }

    public record MensagemResponse(
            UUID id,
            String conteudo,
            LocalDateTime dataEnvio,
            String nomeRemetente,
            UUID idRemetente,
            boolean isGrupo
    ) {
    }
}
