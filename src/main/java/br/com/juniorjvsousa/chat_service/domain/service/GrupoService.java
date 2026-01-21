package br.com.juniorjvsousa.chat_service.domain.service;

import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.GrupoRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Grupo criarGrupo(String nome, List<UUID> idMembros) {
        List<Usuario> membros = usuarioRepository.findAllById(idMembros);
        Grupo grupo = Grupo.builder()
                .nome(nome)
                .membros(membros)
                .build();
        return grupoRepository.save(grupo);
    }

    // metodo para remover membro do grupo
    @Transactional
    public void adicionarMembro(UUID grupoId, UUID usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!grupo.getMembros().contains(usuario)) {
            grupo.getMembros().add(usuario);
            grupoRepository.save(grupo);
        }
    }

    // metodo para remover membro do grupo
    @Transactional
    public void removerMembro(UUID grupoId, UUID usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        grupo.getMembros().remove(usuario);
        grupoRepository.save(grupo);
    }

    public List<Grupo> listarGrupos() {
        return grupoRepository.findAll();
    }

    public Optional<Grupo> buscarPorId(UUID id) {
        return grupoRepository.findById(id);
    }
}
