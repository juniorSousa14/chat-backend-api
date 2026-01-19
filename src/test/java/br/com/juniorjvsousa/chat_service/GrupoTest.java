package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.controller.GrupoController;
import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.GrupoRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.GrupoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

@DisplayName("Testes do Domínio de Grupo")
public class GrupoTest {

    private static final UUID ID_USUARIO = UUID.randomUUID();
    private static final UUID ID_GRUPO = UUID.randomUUID();
    private static final String NOME_GRUPO = "Grupo Java";

    @Nested
    @DisplayName("Testes do Service")
    @ExtendWith(MockitoExtension.class)
    class ServiceTests {

        @Mock
        private GrupoRepository grupoRepository;
        @Mock
        private UsuarioRepository usuarioRepository;
        @InjectMocks
        private GrupoService grupoService;

        private Usuario membro;
        private Grupo grupo;

        @BeforeEach
        void setUp() {
            membro = new Usuario();
            membro.setId(ID_USUARIO);
            membro.setNome("Junior");

            grupo = Grupo.builder()
                    .id(ID_GRUPO)
                    .nome(NOME_GRUPO)
                    .membros(List.of(membro))
                    .build();
        }

        @Test
        @DisplayName("Deve criar grupo corretamente")
        void deveCriarGrupo() {
            List<UUID> ids = List.of(ID_USUARIO);

            when(usuarioRepository.findAllById(ids)).thenReturn(List.of(membro));
            when(grupoRepository.save(any(Grupo.class))).thenReturn(grupo);

            Grupo resultado = grupoService.criarGrupo(NOME_GRUPO, ids);

            assertNotNull(resultado.getId());
            assertEquals(NOME_GRUPO, resultado.getNome());
            assertEquals(1, resultado.getMembros().size());
            verify(grupoRepository).save(any(Grupo.class));
        }

        @Test
        @DisplayName("Deve listar todos os grupos")
        void deveListarGrupos() {
            when(grupoRepository.findAll()).thenReturn(List.of(grupo));

            List<Grupo> lista = grupoService.listarGrupos();

            assertEquals(1, lista.size());
            assertEquals(NOME_GRUPO, lista.get(0).getNome());
        }
    }

    @Nested
    @DisplayName("Testes do Controller")
    @WebMvcTest(GrupoController.class)
    class ControllerTests {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @MockBean
        private GrupoService grupoService;

        private Usuario membro;
        private Grupo grupo;

        @BeforeEach
        void setUp() {
            membro = new Usuario();
            membro.setId(ID_USUARIO);
            membro.setNome("Junior");
            membro.setSenha("123456");

            grupo = Grupo.builder()
                    .id(ID_GRUPO)
                    .nome(NOME_GRUPO)
                    .membros(List.of(membro))
                    .build();
        }

        @Test
        @DisplayName("POST /grupos - Deve retornar 201 e DTO Seguro")
        void deveCriarGrupoViaApi() throws Exception {
            GrupoController.NovoGrupoRequest request =
                    new GrupoController.NovoGrupoRequest(NOME_GRUPO, List.of(ID_USUARIO));

            when(grupoService.criarGrupo(eq(NOME_GRUPO), anyList())).thenReturn(grupo);

            mockMvc.perform(post("/grupos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value(NOME_GRUPO))
                    .andExpect(jsonPath("$.membros[0].nome").value("Junior"))
                    // Garante segurança
                    .andExpect(jsonPath("$.membros[0].senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /grupos - Deve listar convertendo para DTO")
        void deveListarGruposViaApi() throws Exception {
            when(grupoService.listarGrupos()).thenReturn(List.of(grupo));

            mockMvc.perform(get("/grupos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value(NOME_GRUPO));
        }
    }
}
