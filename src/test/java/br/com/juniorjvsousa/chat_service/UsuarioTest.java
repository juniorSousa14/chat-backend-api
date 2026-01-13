package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.UsuarioService;
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
public class UsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve salvar usuário com sucesso quando e-mail não existe")
    void deveSalvarUsuarioComSucesso() {
        Usuario usuarioParaSalvar = Usuario.builder()
                .nome("Teste")
                .email("novo@teste.com")
                .senha("123")
                .build();


        when(usuarioRepository.existsByEmail("novo@teste.com")).thenReturn(false);
        when(usuarioRepository.save(any())).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        Usuario usuarioSalvo = usuarioService.salvar(usuarioParaSalvar);

        assertNotNull(usuarioSalvo.getId());
        assertEquals("novo@teste.com", usuarioSalvo.getEmail());

        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro 409 quando tentar salvar e-mail duplicado")
    void deveLancarErroAoSalvarEmailDuplicado() {
        Usuario usuario = Usuario.builder().email("duplicado@gmail.com").build();

        when(usuarioRepository.existsByEmail("duplicado@gmail.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.salvar(usuario);

        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar usuário quando buscar por ID existente")
    void deveBuscarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(id).nome("Junior").build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        Optional<Usuario> resultado = usuarioService.buscarPorId(id);

        assertTrue(resultado.isPresent());
        assertEquals(id, resultado.get().getId());
    }
}

