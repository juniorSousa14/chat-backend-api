package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.controller.MensagemController;
import br.com.juniorjvsousa.chat_service.domain.entity.Grupo;
import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.MensagemService;
import br.com.juniorjvsousa.chat_service.domain.service.infra.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MensagemController.class)
class MensagemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private MensagemService mensagemService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("POST /mensagens - Deve enviar mensagem com sucesso (201 Created)")
    @WithMockUser
    void deveEnviarMensagemComSucesso() throws Exception {
        UUID grupoId = UUID.randomUUID();
        UUID remetenteId = UUID.randomUUID();
        String conteudo = "Olá mundo, teste corrigido!";


        Usuario remetenteMock = new Usuario();
        remetenteMock.setId(remetenteId);
        remetenteMock.setNome("Junior Dev");

        Grupo grupoMock = new Grupo();
        grupoMock.setId(grupoId);
        grupoMock.setNome("Java Group");

        Mensagem mensagemMock = new Mensagem();
        mensagemMock.setId(UUID.randomUUID());
        mensagemMock.setConteudo(conteudo);
        mensagemMock.setDataEnvio(LocalDateTime.now());
        mensagemMock.setRemetente(remetenteMock);
        mensagemMock.setGrupo(grupoMock);

        when(mensagemService.enviarMensagem(any(), any(), any(), any()))
                .thenReturn(mensagemMock);

        MensagemController.NovaMensagemRequest request =
                new MensagemController.NovaMensagemRequest(grupoId, remetenteId, conteudo, null);

        mockMvc.perform(post("/mensagens")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.conteudo").value(conteudo));

        verify(mensagemService, times(1)).enviarMensagem(
                eq(grupoId),
                eq(conteudo),
                eq(null),
                eq(remetenteId)
        );
    }

    @Test
    @DisplayName("POST /mensagens - Deve retornar Erro 400 se o conteúdo for vazio")
    @WithMockUser
    void deveRetornarBadRequest_QuandoConteudoVazio() throws Exception {

        MensagemController.NovaMensagemRequest request =
                new MensagemController.NovaMensagemRequest(UUID.randomUUID(), UUID.randomUUID(), "", null);

        mockMvc.perform(post("/mensagens")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /mensagens - Deve lançar exceção quando serviço falhar")
    @WithMockUser
    void deveFalhar_QuandoServicoLancarExcecao() throws Exception {
        MensagemController.NovaMensagemRequest request =
                new MensagemController.NovaMensagemRequest(UUID.randomUUID(), UUID.randomUUID(), "Teste", null);

        when(mensagemService.enviarMensagem(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Erro interno simulado"));

        try {
            mockMvc.perform(post("/mensagens")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }
}