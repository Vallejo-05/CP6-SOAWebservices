package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.domain.user.Role;
import br.com.fiap.incidentmanagement.domain.user.User;
import br.com.fiap.incidentmanagement.dto.user.*;
import br.com.fiap.incidentmanagement.infra.exception.BusinessException;
import br.com.fiap.incidentmanagement.infra.exception.ForbiddenOperationException;
import br.com.fiap.incidentmanagement.infra.exception.NotFoundException;
import br.com.fiap.incidentmanagement.repository.RoleRepository;
import br.com.fiap.incidentmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (repository.existsByLogin(request.login())) {
            throw new BusinessException("Já existe um usuário cadastrado com este login.");
        }
        // A senha nunca é armazenada em texto puro
        User user = new User(
                request.name(),
                request.login(),
                passwordEncoder.encode(request.password()),
                findRole(request.role()));
        return new UserResponse(repository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return repository
                .findAll(PageableTranslator.translate(pageable, Map.of("role", "role.name")))
                .map(UserResponse::new);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return new UserResponse(find(id));
    }

    @Transactional
    public UserResponse update(UserUpdateRequest request) {
        User user = find(request.id());
        if (request.login() != null && repository.existsByLoginAndIdNot(request.login(), user.getId())) {
            throw new BusinessException("Já existe um usuário cadastrado com este login.");
        }
        user.update(request.name(), request.login());
        return new UserResponse(user);
    }

    @Transactional
    public UserResponse changeRole(UserRoleRequest request) {
        User user = find(request.id());
        user.changeRole(findRole(request.role()));
        return new UserResponse(user);
    }

    @Transactional
    public void inactivate(Long id, String authenticatedLogin) {
        User user = find(id);
        if (user.getLogin().equals(authenticatedLogin)) {
            throw new BusinessException("Não é permitido inativar o próprio usuário.");
        }
        user.inactivate();
    }

    @Transactional
    public void changeOwnPassword(String authenticatedLogin, PasswordChangeRequest request) {
        User user = repository
                .findByLogin(authenticatedLogin)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
        if (request.id() != null && !request.id().equals(user.getId())) {
            throw new ForbiddenOperationException("Só é permitido alterar a própria senha.");
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta.");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional(readOnly = true)
    public List<String> roles() {
        return roleRepository.findAll(Sort.by("name")).stream().map(Role::getName).toList();
    }

    private User find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
    }

    private Role findRole(String name) {
        return roleRepository
                .findByName(Names.normalize(name))
                .orElseThrow(() -> new BusinessException("Perfil inválido: " + name));
    }
}
