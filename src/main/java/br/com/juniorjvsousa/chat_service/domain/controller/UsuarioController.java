package br.com.juniorjvsousa.chat_service.domain.controller;

import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @Operation(summary = "Cadastrar Usuário", description = "Cria um novo usuário e retorna os dados criados.")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid Usuario usuario) {
        Usuario usuarioSalvo = usuarioService.salvar(usuario);

        UsuarioResponse response = new UsuarioResponse(
                usuarioSalvo.getId(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar Usuario", description = "LIsta todos os usuario criados.")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<Usuario> usuarios = usuarioService.listar();

        List<UsuarioResponse> response = usuarios.stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID", description = "Busca um usuário pelo seu ID.")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id)
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record UsuarioResponse(UUID id, String nome, String email) {
    }


}
