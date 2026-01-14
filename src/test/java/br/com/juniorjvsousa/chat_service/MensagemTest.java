package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.entity.Mensagem;
import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.MensagemRepository;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.MensagemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MensagemTest {
    @Mock
    private MensagemRepository mensagemRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MensagemService mensagemService;

    @Test
    @DisplayName("Deve enviar mensagem com sucesso quando usuário existe")
    void deveEnviarMensagemComSucesso() {
        UUID idUsuario = UUID.randomUUID();
        Usuario remetente = Usuario.builder().id(idUsuario).nome("Junior").build();
        String conteudo = "Olá mundo!";

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(remetente));

        when(mensagemRepository.save(any())).thenAnswer(invocation -> {
            Mensagem m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        Mensagem mensagemSalva = mensagemService.enviarMensagem(idUsuario, conteudo);

        assertNotNull(mensagemSalva.getId());
        assertEquals(conteudo, mensagemSalva.getConteudo());
        assertEquals(remetente, mensagemSalva.getRemetente());
        assertNotNull(mensagemSalva.getDataEnvio());

        verify(mensagemRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário remetente não for encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        UUID idFake = UUID.randomUUID();

        when(usuarioRepository.findById(idFake)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            mensagemService.enviarMensagem(idFake, "Mensagem de teste");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(mensagemRepository, never()).save(any());
    }

}
