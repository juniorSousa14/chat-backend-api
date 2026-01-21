package br.com.juniorjvsousa.chat_service.domain.controller;

import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.service.GrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Tag(name = "Grupos", description = "Gerenciamento de grupos de conversa")
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    @Operation(summary = "Criar Grupo", description = "Cria um novo grupo com uma lista inicial de membros.")
    public ResponseEntity<GrupoResponse> criarGrupo(@RequestBody NovoGrupoRequest request) {
        Grupo grupoSalvo = grupoService.criarGrupo(request.nome(), request.idMembros());
        return ResponseEntity.status(HttpStatus.CREATED).body(converterParaDTO(grupoSalvo));
    }

    @GetMapping
    @Operation(summary = "Listar Grupos", description = "Lista todos os grupos cadastrados.")
    public ResponseEntity<List<GrupoResponse>> listarGrupos() {
        List<Grupo> grupos = grupoService.listarGrupos();
        List<GrupoResponse> response = grupos.stream()
                .map(this::converterParaDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Grupo por ID", description = "Detalhes de um grupo específico.")
    public ResponseEntity<GrupoResponse> buscarPorId(@PathVariable UUID id) {
        return grupoService.buscarPorId(id)
                .map(this::converterParaDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{grupoId}/membros")
    @Operation(summary = "Adicionar Membro", description = "Adiciona um usuário existente ao grupo.")
    public ResponseEntity<Void> adicionarMembro(
            @PathVariable UUID grupoId,
            @RequestBody AdicionarMembroRequest request) {

        grupoService.adicionarMembro(grupoId, request.usuarioId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{grupoId}/membros/{usuarioId}")
    @Operation(summary = "Remover Membro", description = "Remove um usuário do grupo.")
    public ResponseEntity<Void> removerMembro(
            @PathVariable UUID grupoId,
            @PathVariable UUID usuarioId) {

        grupoService.removerMembro(grupoId, usuarioId);

        return ResponseEntity.noContent().build();
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

    // DTOs modelo para o corpo das requisições e respostas

    public record NovoGrupoRequest(
            @NotBlank(message = "Nome do grupo é obrigatório")
            @Schema(example = "Futebol de Quinta", description = "Nome do grupo que será criado")
            String nome,

            @NotEmpty(message = "O grupo deve ter pelo menos um membro inicial")
            @Schema(example = "[\"d215b5f8-0249-4dc5-89a3-51fd148cfb41\"]", description = "Lista de UUIDs dos usuários")
            List<UUID> idMembros) {
    }

    public record AdicionarMembroRequest(
            @NotNull(message = "O ID do usuário é obrigatório")
            @Schema(example = "d215b5f8-0249-4dc5-89a3-51fd148cfb41", description = "UUID do usuário a ser adicionado")
            UUID usuarioId
    ) {
    }

    public record GrupoResponse(UUID id, String nome, List<MembroResponse> membros) {
    }

    public record MembroResponse(UUID id, String nome) {
    }
}