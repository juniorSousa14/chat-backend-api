package br.com.juniorjvsousa.chat_service.domain.service;

import br.com.juniorjvsousa.chat_service.domain.dto.CriarGrupoDTO;
import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.GrupoRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Grupo criarGrupo(CriarGrupoDTO dto) {
        Usuario criador = usuarioRepository.findById(dto.criadorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Grupo grupo = new Grupo();
        grupo.setNome(dto.nome());
        grupo.setMembros(new ArrayList<>());
        grupo.setAdministradores(new HashSet<>());
        grupo.getMembros().add(criador);
        grupo.getAdministradores().add(criador);

        return grupoRepository.save(grupo);
    }

    // metodo para remover membro do grupo
    @Transactional
    public void adicionarMembro(UUID grupoId, UUID novoMembroId, UUID solicitanteId) {
        validarSeUsuarioEAdmin(grupoId, solicitanteId);
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));

        Usuario novoMembro = usuarioRepository.findById(novoMembroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        grupo.getMembros().add(novoMembro);
        grupoRepository.save(grupo);
    }

    // metodo para remover membro do grupo
    @Transactional
    public void removerMembro(UUID grupoId, UUID membroParaRemoverId, UUID solicitanteId) {
        validarSeUsuarioEAdmin(grupoId, solicitanteId);
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));

        Usuario membro = usuarioRepository.findById(membroParaRemoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário alvo não encontrado"));

        grupo.getMembros().remove(membro);
        grupo.getAdministradores().remove(membro);

        grupoRepository.save(grupo);
    }

    public List<Grupo> listarGrupos() {
        return grupoRepository.findAll();
    }

    public Optional<Grupo> buscarPorId(UUID id) {
        return grupoRepository.findById(id);
    }

    // Metodo Auxiliar de Validação
    private void validarSeUsuarioEAdmin(UUID grupoId, UUID usuarioId) {
        boolean isAdm = grupoRepository.existsByIdAndAdministradores_Id(grupoId, usuarioId);
        if (!isAdm) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ação permitida apenas para administradores do grupo.");
        }
    }
}
