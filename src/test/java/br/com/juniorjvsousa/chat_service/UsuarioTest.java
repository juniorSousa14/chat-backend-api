package br.com.juniorjvsousa.chat_service;

import br.com.juniorjvsousa.chat_service.domain.entity.Usuario;
import br.com.juniorjvsousa.chat_service.domain.repository.UsuarioRepository;
import br.com.juniorjvsousa.chat_service.domain.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Usuario criarUsuarioMock() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Junior Teste");
        usuario.setEmail("junior@teste.com");
        usuario.setSenha("123456");
        return usuario;
    }

    @Nested
    @DisplayName("Testes do método Salvar")
    class SalvarTests {

        @Test
        @DisplayName("Deve salvar usuário com sucesso quando email não existe")
        void deveSalvarUsuarioComSucesso() {

            Usuario usuarioEntrada = new Usuario();
            usuarioEntrada.setEmail("novo@email.com");
            usuarioEntrada.setSenha("123456");

            Usuario usuarioSalvo = criarUsuarioMock();
            usuarioSalvo.setSenha("senhaCriptografada");

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

            when(passwordEncoder.encode("123456")).thenReturn("senhaCriptografada");

            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

            Usuario resultado = usuarioService.salvar(usuarioEntrada);

            assertNotNull(resultado);
            assertEquals("senhaCriptografada", resultado.getSenha());

            verify(usuarioRepository, times(1)).existsByEmail("novo@email.com");
            verify(passwordEncoder, times(1)).encode("123456");
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar erro (Conflict) quando email já existe")
        void deveLancarErroEmailDuplicado() {

            Usuario usuarioEntrada = new Usuario();
            usuarioEntrada.setEmail("existente@email.com");

            when(usuarioRepository.existsByEmail("existente@email.com")).thenReturn(true);

            assertThrows(ResponseStatusException.class, () -> {
                usuarioService.salvar(usuarioEntrada);
            });

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca")
    class BuscarTests {

        @Test
        @DisplayName("Deve retornar Optional com usuário quando ID existe")
        void deveEncontrarUsuarioPorId() {
            UUID id = UUID.randomUUID();
            Usuario usuario = criarUsuarioMock();
            usuario.setId(id);

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            Optional<Usuario> resultado = usuarioService.buscarPorId(id);

            assertTrue(resultado.isPresent());
            assertEquals(id, resultado.get().getId());
        }

        @Test
        @DisplayName("Deve retornar Optional vazio quando ID não existe")
        void deveRetornarVazioSeIdNaoExiste() {
            UUID id = UUID.randomUUID();
            when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Usuario> resultado = usuarioService.buscarPorId(id);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve listar todos os usuários")
        void deveListarUsuarios() {
            when(usuarioRepository.findAll()).thenReturn(List.of(criarUsuarioMock()));

            List<Usuario> lista = usuarioService.listar();

            assertFalse(lista.isEmpty());
            assertEquals(1, lista.size());
        }
    }

    @Nested
    @DisplayName("Outros Testes")
    class OutrosTests {
        @Test
        @DisplayName("Deve verificar se usuário pertence ao grupo")
        void deveVerificarGrupo() {
            UUID userId = UUID.randomUUID();
            UUID grupoId = UUID.randomUUID();

            when(usuarioRepository.existsByIdAndGrupos_Id(userId, grupoId)).thenReturn(true);

            boolean pertence = usuarioService.usuarioPertenceAoGrupo(userId, grupoId);

            assertTrue(pertence);
        }
    }
}