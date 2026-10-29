package br.com.fiap.incidentmanagement.dto.user;

import br.com.fiap.incidentmanagement.domain.user.User;

// A senha nunca é devolvida pela API
public record UserResponse(
        Long id,
        String name,
        String login,
        String role,
        boolean active) {
    public UserResponse(User user) {
        this(user.getId(), user.getName(), user.getLogin(), user.getRole().getName(), user.isActive());
    }
}
