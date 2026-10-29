package br.com.fiap.incidentmanagement.user;

import br.com.fiap.incidentmanagement.domain.user.Role;
import br.com.fiap.incidentmanagement.domain.user.User;
import br.com.fiap.incidentmanagement.dto.user.PasswordChangeRequest;
import br.com.fiap.incidentmanagement.dto.user.UserCreateRequest;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.ForbiddenOperationException;
import br.com.fiap.incidentmanagement.repository.RoleRepository;
import br.com.fiap.incidentmanagement.repository.UserRepository;
import br.com.fiap.incidentmanagement.service.UserService;
import br.com.fiap.incidentmanagement.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository repository;

    @Mock
    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("A senha é armazenada criptografada com BCrypt")
    void createEncodesPassword() {
        when(repository.existsByLogin("maria@escola.com")).thenReturn(false);
        when(roleRepository.findByName("PROFESSOR")).thenReturn(Optional.of(new Role(1L, "PROFESSOR")));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new UserCreateRequest("Maria", "maria@escola.com", "senha123", "professor"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isNotEqualTo("senha123");
        assertThat(passwordEncoder.matches("senha123", captor.getValue().getPassword())).isTrue();
    }

    @Test
    @DisplayName("Não permite login duplicado")
    void createDuplicatedLogin() {
        when(repository.existsByLogin("admin@escola.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new UserCreateRequest("Outro", "admin@escola.com", "senha123", "PROFESSOR")))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não permite perfil inexistente")
    void createWithInvalidRole() {
        when(repository.existsByLogin("maria@escola.com")).thenReturn(false);
        when(roleRepository.findByName("DIRETOR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                new UserCreateRequest("Maria", "maria@escola.com", "senha123", "DIRETOR")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Perfil inválido");
    }

    @Test
    @DisplayName("Não permite inativar o próprio usuário")
    void inactivateSelf() {
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.user(1L, "admin@escola.com", "ADMINISTRATIVO")));

        assertThatThrownBy(() -> service.inactivate(1L, "admin@escola.com")).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Inativa outro usuário (exclusão lógica)")
    void inactivateOther() {
        User other = TestData.user(5L, "maria@escola.com", "PROFESSOR");
        when(repository.findById(5L)).thenReturn(Optional.of(other));

        service.inactivate(5L, "admin@escola.com");

        assertThat(other.isActive()).isFalse();
        assertThat(other.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Troca a própria senha quando a senha atual está correta")
    void changeOwnPassword() {
        User user = TestData.user(4L, "professor@escola.com", "PROFESSOR");
        user.changePassword(passwordEncoder.encode("123456"));
        when(repository.findByLogin("professor@escola.com")).thenReturn(Optional.of(user));

        service.changeOwnPassword("professor@escola.com", new PasswordChangeRequest(4L, "123456", "novasenha"));

        assertThat(passwordEncoder.matches("novasenha", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Rejeita a troca de senha com senha atual incorreta")
    void changeOwnPasswordWrongOldPassword() {
        User user = TestData.user(4L, "professor@escola.com", "PROFESSOR");
        user.changePassword(passwordEncoder.encode("123456"));
        when(repository.findByLogin("professor@escola.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changeOwnPassword(
                "professor@escola.com", new PasswordChangeRequest(4L, "errada", "novasenha")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Não permite alterar a senha de outro usuário")
    void changeOtherUsersPassword() {
        when(repository.findByLogin("professor@escola.com"))
                .thenReturn(Optional.of(TestData.user(4L, "professor@escola.com", "PROFESSOR")));

        assertThatThrownBy(() -> service.changeOwnPassword(
                "professor@escola.com", new PasswordChangeRequest(1L, "123456", "novasenha")))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
