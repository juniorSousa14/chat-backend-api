package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.controller.MensagemController;
import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.GrupoRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.MensagemRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.MensagemService;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Testes do Domínio de Mensagem")
public class MensagemTest {

    private static final UUID ID_REMETENTE = UUID.randomUUID();
    private static final UUID ID_DESTINO = UUID.randomUUID();
    private static final UUID ID_GRUPO = UUID.randomUUID();

    @Nested
    @DisplayName("Testes do Service")
    @ExtendWith(MockitoExtension.class)
    class ServiceTests {

        @Mock
        private MensagemRepository mensagemRepository;
        @Mock
        private UsuarioRepository usuarioRepository;
        @Mock
        private GrupoRepository grupoRepository;
        @InjectMocks
        private MensagemService mensagemService;

        private Usuario remetente;
        private Usuario destinatario;
        private Mensagem mensagemSalva;

        @BeforeEach
        void setUp() {
            remetente = Usuario.builder().id(ID_REMETENTE).nome("Junior").build();
            destinatario = Usuario.builder().id(ID_DESTINO).nome("Jose").build();

            mensagemSalva = Mensagem.builder()
                    .id(UUID.randomUUID())
                    .conteudo("Olá mundo!")
                    .remetente(remetente)
                    .usuarioDestino(destinatario)
                    .dataEnvio(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Deve enviar mensagem privada com sucesso")
        void deveEnviarMensagemComSucesso() {
            when(usuarioRepository.findById(ID_REMETENTE)).thenReturn(Optional.of(remetente));
            when(usuarioRepository.findById(ID_DESTINO)).thenReturn(Optional.of(destinatario));
            when(mensagemRepository.save(any(Mensagem.class))).thenReturn(mensagemSalva);

            Mensagem resultado = mensagemService.enviarMensagem(ID_REMETENTE, "Olá mundo!", null, ID_DESTINO);

            assertNotNull(resultado.getId());
            assertEquals("Olá mundo!", resultado.getConteudo());
            assertEquals(remetente, resultado.getRemetente());
            verify(mensagemRepository).save(any(Mensagem.class));
        }

        @Test
        @DisplayName("Deve lançar erro 404 se remetente não existir")
        void deveFalharSeRemetenteNaoExiste() {
            when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(ResponseStatusException.class, () ->
                    mensagemService.enviarMensagem(UUID.randomUUID(), "Teste", null, null)
            );
        }
    }

    @Nested
    @DisplayName("Testes do Controller")
    @WebMvcTest(MensagemController.class)
    class ControllerTests {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @MockBean
        private MensagemService mensagemService;

        private Mensagem mensagem;

        @BeforeEach
        void setUp() {
            Usuario remetente = Usuario.builder().id(ID_REMETENTE).nome("Junior").senha("123").build();

            mensagem = Mensagem.builder()
                    .id(UUID.randomUUID())
                    .conteudo("Oi API!")
                    .remetente(remetente)
                    .dataEnvio(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("POST /mensagens - Deve retornar 201 e DTO Seguro")
        void deveEnviarMensagemViaApi() throws Exception {
            MensagemController.NovaMensagemRequest request =
                    new MensagemController.NovaMensagemRequest(ID_REMETENTE, ID_DESTINO, "Oi API!", null);

            when(mensagemService.enviarMensagem(eq(ID_REMETENTE), anyString(), eq(null), eq(ID_DESTINO)))
                    .thenReturn(mensagem);

            mockMvc.perform(post("/mensagens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.conteudo").value("Oi API!"))
                    .andExpect(jsonPath("$.nomeRemetente").value("Junior"))
                    .andExpect(jsonPath("$.senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /mensagens/privada - Deve listar DTOs")
        void deveListarPrivadasViaApi() throws Exception {
            when(mensagemService.listarMensagensPorUsuario(ID_REMETENTE, ID_DESTINO))
                    .thenReturn(List.of(mensagem));

            mockMvc.perform(get("/mensagens/privada")
                            .param("usuarioId", ID_REMETENTE.toString())
                            .param("usuario2Id", ID_DESTINO.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].conteudo").value("Oi API!"));
        }
    }
}