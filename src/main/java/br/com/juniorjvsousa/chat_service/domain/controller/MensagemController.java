package br.com.juniorjvsousa.chat_service.domain.controller;

import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.service.MensagemService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mensagens")
@RequiredArgsConstructor
@Tag(name = "Mensagens", description = "Envio e histórico de mensagens")
public class MensagemController {

    private final MensagemService mensagemService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<MensagemResponse> enviarMensagem(@RequestBody @Valid NovaMensagemRequest request) {
        Mensagem mensagem = mensagemService.enviarMensagem(request.usuarioId(), request.conteudo(), request.grupoId(), request.usuario2Id());

        MensagemResponse responseDTO = converterParaDTO(mensagem);

        if (mensagem.getGrupo() != null) {
            String destino = "/topico/grupo/" + mensagem.getGrupo().getId();
            messagingTemplate.convertAndSend(destino, responseDTO);

        } else if (mensagem.getUsuarioDestino() != null) {
            String destino = "/topico/usuario/" + mensagem.getUsuarioDestino().getId();
            messagingTemplate.convertAndSend(destino, responseDTO);

            messagingTemplate.convertAndSend("/topico/usuario/" + mensagem.getRemetente().getId(), responseDTO);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/privada")
    public ResponseEntity<List<MensagemResponse>> listarMensagensEntreUsuarioEUsusario2(@RequestParam UUID usuarioId, @RequestParam UUID usuario2Id) {

        List<Mensagem> mensagens = mensagemService.listarMensagensPorUsuario(usuarioId, usuario2Id);

        List<MensagemResponse> resposta = mensagens.stream().map(this::converterParaDTO).toList();

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<MensagemResponse>> listarMensagensPorGrupo(@PathVariable UUID grupoId) {
        List<Mensagem> mensagens = mensagemService.listarMensagensDoGrupo(grupoId);

        List<MensagemResponse> resposta = mensagens.stream().map(this::converterParaDTO).toList();

        return ResponseEntity.ok(resposta);
    }

    private MensagemResponse converterParaDTO(Mensagem m) {
        return new MensagemResponse(m.getId(), m.getConteudo(), m.getDataEnvio(), m.getRemetente().getNome(), m.getRemetente().getId(), m.getGrupo() != null);
    }

    // DTOs COM EXEMPLOS PARA O SWAGGER

    public record NovaMensagemRequest(
            @NotNull(message = "ID do remetente é obrigatório") @Schema(example = "", description = "ID de quem está enviando") UUID usuarioId,

            @Schema(example = "", description = "ID do destinatário (apenas para mensagens privadas)") UUID usuario2Id,

            @NotBlank(message = "O conteúdo da mensagem não pode estar vazio") @Schema(example = "Olá! Vamos marcar aquela reunião?", description = "Texto da mensagem") String conteudo,

            @Schema(example = "null", description = "ID do grupo (apenas para mensagens de grupo)") UUID grupoId) {
    }

    public record MensagemResponse(UUID id, String conteudo, LocalDateTime dataEnvio, String nomeRemetente,
                                   UUID idRemetente, boolean isGrupo) {
    }
}