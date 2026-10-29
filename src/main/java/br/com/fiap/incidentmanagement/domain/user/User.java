package br.com.fiap.incidentmanagement.domain.user;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity(name = "User")
@Table(name = "usuarios")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;

    private String login;

    @Column(name = "senha")
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "perfil_id")
    private Role role;

    @Column(name = "ativo")
    private boolean active = true;

    // A senha recebida aqui já deve estar criptografada (BCrypt)
    public User(String name, String login, String encodedPassword, Role role) {
        this.name = name;
        this.login = login;
        this.password = encodedPassword;
        this.role = role;
    }

    public void update(String name, String login) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (login != null && !login.isBlank()) {
            this.login = login;
        }
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // Exclusão lógica: o usuário é inativado e não consegue mais se autenticar
    public void inactivate() {
        this.active = false;
    }

    public boolean hasRole(String roleName) {
        return role != null && role.getName().equals(roleName);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
