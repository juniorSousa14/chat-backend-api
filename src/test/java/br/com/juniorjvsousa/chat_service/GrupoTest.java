package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.controller.GrupoController;
import br.com.juniorjvsousa.chat_service.domain.dto.CriarGrupoDTO;
import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.GrupoRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.GrupoService;
import br.com.juniorjvsousa.chat_service.domain.service.infra.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GrupoTest {

    @Nested
    @DisplayName("Testes do Service (Regras de Negócio e Admins)")
    class ServiceTests {
        @Mock
        private GrupoRepository grupoRepository;
        @Mock
        private UsuarioRepository usuarioRepository;
        @InjectMocks
        private GrupoService grupoService;

        @Test
        void deveCriarGrupo() {
            UUID criadorId = UUID.randomUUID();
            Usuario criador = new Usuario();
            criador.setId(criadorId);
            CriarGrupoDTO dto = new CriarGrupoDTO("Grupo Teste", criadorId, null);

            when(usuarioRepository.findById(criadorId)).thenReturn(Optional.of(criador));

            when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> {
                Grupo g = inv.getArgument(0);
                g.setId(UUID.randomUUID());

                if (g.getMembros() == null) g.setMembros(new ArrayList<>());
                if (g.getAdministradores() == null) g.setAdministradores(new HashSet<>());

                return g;
            });

            Grupo resultado = grupoService.criarGrupo(dto);
            assertNotNull(resultado.getId());
        }

        @Test
        void deveAdicionarMembroSendoAdmin() {
            UUID grupoId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            UUID novoMembroId = UUID.randomUUID();

            Grupo grupo = new Grupo();
            grupo.setId(grupoId);
            grupo.setMembros(new ArrayList<>());
            grupo.setAdministradores(new HashSet<>());

            Usuario novoMembro = new Usuario();
            novoMembro.setId(novoMembroId);

            when(grupoRepository.existsByIdAndAdministradores_Id(grupoId, adminId)).thenReturn(true);
            when(grupoRepository.findById(grupoId)).thenReturn(Optional.of(grupo));
            when(usuarioRepository.findById(novoMembroId)).thenReturn(Optional.of(novoMembro));

            grupoService.adicionarMembro(grupoId, novoMembroId, adminId);

            assertTrue(grupo.getMembros().contains(novoMembro));
        }

        @Test
        void naoDeveAdicionarMembroSeNaoForAdmin() {
            UUID grupoId = UUID.randomUUID();
            UUID usuarioComumId = UUID.randomUUID();
            UUID novoMembroId = UUID.randomUUID();
            when(grupoRepository.existsByIdAndAdministradores_Id(grupoId, usuarioComumId)).thenReturn(false);
            assertThrows(ResponseStatusException.class, () -> grupoService.adicionarMembro(grupoId, novoMembroId, usuarioComumId));
        }

        @Test
        void deveRemoverMembroSendoAdmin() {
            UUID grupoId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            UUID membroAlvoId = UUID.randomUUID();

            Usuario membroAlvo = new Usuario();
            membroAlvo.setId(membroAlvoId);

            Grupo grupo = new Grupo();
            grupo.setId(grupoId);
            grupo.setMembros(new ArrayList<>());
            grupo.setAdministradores(new HashSet<>());
            grupo.getMembros().add(membroAlvo);

            when(grupoRepository.existsByIdAndAdministradores_Id(grupoId, adminId)).thenReturn(true);
            when(grupoRepository.findById(grupoId)).thenReturn(Optional.of(grupo));
            when(usuarioRepository.findById(membroAlvoId)).thenReturn(Optional.of(membroAlvo));

            grupoService.removerMembro(grupoId, membroAlvoId, adminId);
            assertFalse(grupo.getMembros().contains(membroAlvo));
        }
    }

    @Nested
    @DisplayName("Testes do Controller (API)")
    @WebMvcTest(GrupoController.class)
    @AutoConfigureMockMvc
    class ControllerTests {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private GrupoService grupoService;
        @MockBean
        private TokenService tokenService;
        @MockBean
        private UsuarioRepository usuarioRepository;

        private Authentication getAuthentication(UUID userId) {
            Usuario usuario = new Usuario();
            usuario.setId(userId);
            usuario.setNome("Usuario Teste");
            return new UsernamePasswordAuthenticationToken(usuario, null, Collections.emptyList());
        }

        @Test
        @DisplayName("POST /grupos - Deve criar grupo")
        void deveCriarGrupoViaApi() throws Exception {
            UUID criadorId = UUID.randomUUID();
            NovoGrupoRequest request = new NovoGrupoRequest("Devs Java", List.of(UUID.randomUUID()));

            Usuario criador = new Usuario();
            criador.setId(criadorId);

            Grupo grupoRetornado = new Grupo();
            grupoRetornado.setId(UUID.randomUUID());
            grupoRetornado.setNome("Devs Java");
            grupoRetornado.setMembros(new ArrayList<>());
            grupoRetornado.setAdministradores(new HashSet<>());
            grupoRetornado.getMembros().add(criador);

            when(grupoService.criarGrupo(any(CriarGrupoDTO.class))).thenReturn(grupoRetornado);

            mockMvc.perform(post("/grupos").with(csrf()).with(authentication(getAuthentication(criadorId))).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Devs Java"));
        }

        @Test
        @DisplayName("POST /grupos/{id}/membros - Deve passar ID do usuário logado")
        void deveAdicionarMembroViaApi() throws Exception {
            UUID grupoId = UUID.randomUUID();
            UUID novoMembroId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();

            AdicionarMembroRequest request = new AdicionarMembroRequest(novoMembroId);

            mockMvc.perform(post("/grupos/{id}/membros", grupoId).with(csrf()).with(authentication(getAuthentication(adminId))).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

            verify(grupoService).adicionarMembro(eq(grupoId), eq(novoMembroId), eq(adminId));
        }

        @Test
        @DisplayName("DELETE /grupos/{id}/membros/{userId} - Deve remover usuario")
        void deveRemoverMembroViaApi() throws Exception {
            UUID grupoId = UUID.randomUUID();
            UUID membroAlvoId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();

            mockMvc.perform(delete("/grupos/{id}/membros/{userId}", grupoId, membroAlvoId).with(csrf()).with(authentication(getAuthentication(adminId)))).andExpect(status().isNoContent());

            verify(grupoService).removerMembro(eq(grupoId), eq(membroAlvoId), eq(adminId));
        }

        record NovoGrupoRequest(String nome, List<UUID> idMembros) {
        }

        record AdicionarMembroRequest(UUID usuarioId) {
        }
    }
}