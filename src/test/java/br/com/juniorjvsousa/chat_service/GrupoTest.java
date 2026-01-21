package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.controller.GrupoController;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GrupoTest {

    @Nested
    @DisplayName("Testes do Service (Regras de Negócio)")
    @ExtendWith(MockitoExtension.class)
    class ServiceTests {
        @Mock
        private GrupoRepository grupoRepository;
        @Mock
        private UsuarioRepository usuarioRepository;
        @InjectMocks
        private GrupoService grupoService;

        @Test
        void deveCriarGrupo() {
            UUID idUsuario = UUID.randomUUID();
            Usuario u1 = Usuario.builder().id(idUsuario).nome("User").build();
            List<UUID> ids = List.of(idUsuario);
            when(usuarioRepository.findAllById(ids)).thenReturn(List.of(u1));
            when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> {
                Grupo g = inv.getArgument(0);
                g.setId(UUID.randomUUID());
                return g;
            });
            Grupo resultado = grupoService.criarGrupo("Grupo Teste", ids);
            assertNotNull(resultado.getId());
        }

        @Test
        void deveAdicionarMembro() {
            UUID grupoId = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Grupo grupo = Grupo.builder().id(grupoId).membros(new ArrayList<>()).build();
            Usuario usuario = Usuario.builder().id(usuarioId).build();

            when(grupoRepository.findById(grupoId)).thenReturn(Optional.of(grupo));
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

            grupoService.adicionarMembro(grupoId, usuarioId);

            assertTrue(grupo.getMembros().contains(usuario));
        }

        @Test
        void deveRemoverMembro() {
            UUID grupoId = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Usuario usuario = Usuario.builder().id(usuarioId).build();
            List<Usuario> membros = new ArrayList<>();
            membros.add(usuario);
            Grupo grupo = Grupo.builder().id(grupoId).membros(membros).build();

            when(grupoRepository.findById(grupoId)).thenReturn(Optional.of(grupo));
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

            grupoService.removerMembro(grupoId, usuarioId);

            assertFalse(grupo.getMembros().contains(usuario));
        }
    }

    @Nested
    @DisplayName("Testes do Controller (API)")
    @WebMvcTest(GrupoController.class)
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

        @Test
        @DisplayName("POST /grupos/{id}/membros - Deve adicionar usuario")
        @WithMockUser
        void deveAdicionarMembroViaApi() throws Exception {
            UUID grupoId = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            GrupoController.AdicionarMembroRequest request =
                    new GrupoController.AdicionarMembroRequest(usuarioId);

            mockMvc.perform(post("/grupos/{id}/membros", grupoId)
                            .with(csrf()) // <--- AQUI: Injeta o token CSRF
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(grupoService).adicionarMembro(grupoId, usuarioId);
        }

        @Test
        @DisplayName("DELETE /grupos/{id}/membros/{userId} - Deve remover usuario")
        @WithMockUser
        void deveRemoverMembroViaApi() throws Exception {
            UUID grupoId = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();

            mockMvc.perform(delete("/grupos/{id}/membros/{userId}", grupoId, usuarioId)
                            .with(csrf())) // <--- AQUI TAMBÉM
                    .andExpect(status().isNoContent());

            verify(grupoService).removerMembro(grupoId, usuarioId);
        }

        @Test
        @DisplayName("POST /grupos - Deve criar grupo")
        @WithMockUser
        void deveCriarGrupoViaApi() throws Exception {
            UUID userId = UUID.randomUUID();
            GrupoController.NovoGrupoRequest request =
                    new GrupoController.NovoGrupoRequest("Devs Java", List.of(userId));

            Usuario membro = new Usuario();
            membro.setId(userId);
            membro.setNome("Junior");

            Grupo grupoRetornado = new Grupo();
            grupoRetornado.setId(UUID.randomUUID());
            grupoRetornado.setNome("Devs Java");
            grupoRetornado.setMembros(List.of(membro));

            when(grupoService.criarGrupo(eq("Devs Java"), anyList())).thenReturn(grupoRetornado);

            mockMvc.perform(post("/grupos")
                            .with(csrf()) // <--- E AQUI
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value("Devs Java"));
        }
    }
}