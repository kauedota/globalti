package br.com.globalti.prova.security;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Essa classe serve para representar o usuário logado dentro do Spring Security.
 * Ela guarda só o necessário (id, email, senha com hash e role) e é o que os
 * services recebem para decidir quem pode mexer em quê.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String senha;
    private final Role role;

    public UserPrincipal(Long id, String email, String senha, Role role) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    public static UserPrincipal de(User user) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getSenha(), user.getRole());
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // O prefixo ROLE_ é a convenção do Spring Security.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
