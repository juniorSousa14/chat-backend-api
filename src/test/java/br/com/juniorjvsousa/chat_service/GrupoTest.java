package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.controller.GrupoController;
import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.GrupoRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.GrupoService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GrupoTest {

    @Nested
    @DisplayName("Testes do Service (Lógica de Negócio)")
    @ExtendWith(MockitoExtension.class)
    class ServiceTests {

        @Mock
        private GrupoRepository grupoRepository;

        @Mock
        private UsuarioRepository usuarioRepository;

        @InjectMocks
        private GrupoService grupoService;

        @Test
        @DisplayName("Deve criar grupo salvando membros corretamente")
        void deveCriarGrupo() {
            UUID id1 = UUID.randomUUID();
            Usuario u1 = new Usuario();
            u1.setId(id1);
            List<UUID> ids = List.of(id1);

            when(usuarioRepository.findAllById(ids)).thenReturn(List.of(u1));
            when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> {
                Grupo g = inv.getArgument(0);
                g.setId(UUID.randomUUID());
                return g;
            });

            Grupo resultado = grupoService.criarGrupo("Grupo Teste", ids);

            assertNotNull(resultado.getId());
            assertEquals("Grupo Teste", resultado.getNome());
            assertEquals(1, resultado.getMembros().size());
            verify(grupoRepository).save(any(Grupo.class));
        }

        @Test
        @DisplayName("Deve retornar todos os grupos")
        void deveListarGrupos() {
            when(grupoRepository.findAll()).thenReturn(List.of(new Grupo(), new Grupo()));
            List<Grupo> lista = grupoService.listarGrupos();
            assertEquals(2, lista.size());
        }
    }

    @Nested
    @DisplayName("Testes do Controller (API REST)")
    @WebMvcTest(GrupoController.class)
    class ControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private GrupoService grupoService;

        @Test
        @DisplayName("POST /grupos - Deve retornar 201 e JSON Seguro (DTO)")
        void deveCriarGrupoViaApi() throws Exception {
            UUID userId = UUID.randomUUID();
            GrupoController.NovoGrupoRequest request =
                    new GrupoController.NovoGrupoRequest("Devs Java", List.of(userId));

            Usuario membro = new Usuario();
            membro.setId(userId);
            membro.setNome("Junior");
            membro.setSenha("123456");

            Grupo grupoRetornado = new Grupo();
            grupoRetornado.setId(UUID.randomUUID());
            grupoRetornado.setNome("Devs Java");
            grupoRetornado.setMembros(List.of(membro));

            when(grupoService.criarGrupo(eq("Devs Java"), anyList())).thenReturn(grupoRetornado);

            mockMvc.perform(post("/grupos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value("Devs Java"))
                    .andExpect(jsonPath("$.membros[0].nome").value("Junior"))
                    .andExpect(jsonPath("$.membros[0].senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /grupos - Deve listar convertendo para DTO")
        void deveListarGruposViaApi() throws Exception {
            Grupo g = new Grupo();
            g.setId(UUID.randomUUID());
            g.setNome("Grupo API");
            g.setMembros(List.of());

            when(grupoService.listarGrupos()).thenReturn(List.of(g));

            mockMvc.perform(get("/grupos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value("Grupo API"));
        }
    }
}
