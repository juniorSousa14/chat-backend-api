package br.com.juniorjvsousa.chat_service.domain.controller;

import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.service.MensagemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mensagens")
@RequiredArgsConstructor
public class MensagemController {

    private final MensagemService mensagemService;

    @PostMapping
    public ResponseEntity<Mensagem> enviarMensagem(@RequestBody NovaMensagemRequest request) {
        Mensagem mensagem = mensagemService.enviarMensagem(request.usuarioId(), request.conteudo());
        return new ResponseEntity<>(mensagem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Mensagem>> listar() {
        return ResponseEntity.ok(mensagemService.listarTodasMensagens());

    }

    public record NovaMensagemRequest(UUID usuarioId, String conteudo) {
    }

}
