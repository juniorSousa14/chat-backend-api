package br.com.juniorjvsousa.chat_service.domain.controller;


import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.service.GrupoService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    public ResponseEntity<GrupoResponse> criarGrupo(@RequestBody NovoGrupoRequest request) {
        Grupo grupoSalvo = grupoService.criarGrupo(request.nome(), request.idMembros());

        return ResponseEntity.status(HttpStatus.CREATED).body(converterParaDTO(grupoSalvo));
    }

    @GetMapping
    public ResponseEntity<List<GrupoResponse>> listarGrupos() {
        List<Grupo> grupos = grupoService.listarGrupos();

        List<GrupoResponse> response = grupos.stream()
                .map(this::converterParaDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponse> buscarPorId(@PathVariable UUID id) {
        return grupoService.buscarPorId(id)
                .map(this::converterParaDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private GrupoResponse converterParaDTO(Grupo grupo) {
        List<MembroResponse> membrosSeguros = grupo.getMembros().stream()
                .map(u -> new MembroResponse(u.getId(), u.getNome()))
                .collect(Collectors.toList());

        return new GrupoResponse(
                grupo.getId(),
                grupo.getNome(),
                membrosSeguros
        );
    }

    public record NovoGrupoRequest(
            @NotBlank(message = "Nome do grupo é obrigatório")
            String nome,

            @NotEmpty(message = "O grupo deve ter pelo menos um membro inicial")
            List<UUID> idMembros) {
    }

    public record GrupoResponse(UUID id, String nome, List<MembroResponse> membros) {
    }

    public record MembroResponse(UUID id, String nome) {
    }
}
