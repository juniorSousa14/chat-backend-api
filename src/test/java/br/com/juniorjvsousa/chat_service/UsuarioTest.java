package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

@DisplayName("Testes do Domínio de Usuário")
public class UsuarioTest {

    @Nested
    @DisplayName("Testes do Service")
    @ExtendWith(MockitoExtension.class)
    class ServiceTests {

        private final UUID ID_USUARIO = UUID.randomUUID();
        @Mock
        private UsuarioRepository usuarioRepository;
        @InjectMocks
        private UsuarioService usuarioService;
        private Usuario usuarioPadrao;

        @BeforeEach
        void setUp() {
            usuarioPadrao = Usuario.builder()
                    .id(ID_USUARIO)
                    .nome("Junior Sousa")
                    .email("junior@teste.com")
                    .senha("123456")
                    .build();
        }

        @Test
        @DisplayName("Deve salvar usuário com sucesso")
        void deveSalvarUsuarioComSucesso() {
            // Arrange
            when(usuarioRepository.existsByEmail(usuarioPadrao.getEmail())).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPadrao);

            // Act
            Usuario resultado = usuarioService.salvar(usuarioPadrao);

            // Assert
            assertNotNull(resultado.getId());
            assertEquals(usuarioPadrao.getEmail(), resultado.getEmail());
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar salvar e-mail duplicado")
        void deveLancarErroEmailDuplicado() {
            // Arrange
            when(usuarioRepository.existsByEmail(usuarioPadrao.getEmail())).thenReturn(true);

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    usuarioService.salvar(usuarioPadrao)
            );

            // Assert
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve buscar por ID com sucesso")
        void deveBuscarPorId() {
            when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuarioPadrao));

            Optional<Usuario> resultado = usuarioService.buscarPorId(ID_USUARIO);

            assertTrue(resultado.isPresent());
            assertEquals(ID_USUARIO, resultado.get().getId());
        }
    }
}