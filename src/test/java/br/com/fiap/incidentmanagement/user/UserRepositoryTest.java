package br.com.fiap.incidentmanagement.user;

import br.com.fiap.incidentmanagement.domain.user.Role;
import br.com.fiap.incidentmanagement.domain.user.User;
import br.com.fiap.incidentmanagement.repository.RoleRepository;
import br.com.fiap.incidentmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    UserRepository repository;

    @Autowired
    RoleRepository roleRepository;

    @Test
    @DisplayName("As migrations criam os 4 perfis e um usuário por perfil, com senha criptografada")
    void seedData() {
        assertThat(roleRepository.findAll()).extracting(Role::getName)
                .containsExactlyInAnyOrder(Role.PROFESSOR, Role.ANALISTA, Role.COORDENADOR, Role.ADMINISTRATIVO);

        User admin = repository.findByLogin("admin@escola.com").orElseThrow();
        assertThat(admin.hasRole(Role.ADMINISTRATIVO)).isTrue();
        assertThat(new BCryptPasswordEncoder().matches("123456", admin.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Verifica login duplicado desconsiderando o próprio usuário")
    void existsByLoginAndIdNot() {
        Role professor = roleRepository.findByName(Role.PROFESSOR).orElseThrow();
        User maria = repository.save(new User("Maria", "maria@escola.com", "hash", professor));

        assertThat(repository.existsByLogin("maria@escola.com")).isTrue();
        assertThat(repository.existsByLoginAndIdNot("maria@escola.com", maria.getId())).isFalse();
        assertThat(repository.existsByLoginAndIdNot("admin@escola.com", maria.getId())).isTrue();
    }
}
