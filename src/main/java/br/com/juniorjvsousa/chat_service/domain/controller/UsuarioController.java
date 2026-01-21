package br.com.juniorjvsousa.chat_service.domain.controller;

import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários do chat")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar Usuário", description = "Cria um novo usuário.")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid DadosCadastroUsuario dados) {

        Usuario usuarioParaSalvar = Usuario.builder()
                .nome(dados.nome())
                .email(dados.email())
                .telefone(dados.telefone())
                .senha(dados.senha())
                .build();

        Usuario usuarioSalvo = usuarioService.salvar(usuarioParaSalvar);

        UsuarioResponse response = new UsuarioResponse(
                usuarioSalvo.getId(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getTelefone()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar Usuario", description = "Lista todos os usuários criados.")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<Usuario> usuarios = usuarioService.listar();

        List<UsuarioResponse> response = usuarios.stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getTelefone()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Busca um usuário pelo seu ID.")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id)
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getTelefone()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record UsuarioResponse(UUID id, String nome, String email, String telefone) {
    }

    public record DadosCadastroUsuario(
            @NotBlank
            @Schema(example = "Test")
            String nome,

            @NotBlank
            @Email
            @Schema(example = "Test@email.com")
            String email,

            @NotBlank
            @Schema(example = "11999998888")
            String telefone,

            @NotBlank
            @Size(min = 6)
            @Schema(example = "123456")
            String senha
    ) {
    }
}