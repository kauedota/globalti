package br.com.globalti.prova.security;

import br.com.globalti.prova.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Essa classe serve para o Spring Security achar o usuário no banco.
 * Ela é usada no login (por email) e no filtro do JWT (por id).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Usado pelo login. O "username" aqui é o email.
    @Override
    public UserPrincipal loadUserByUsername(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(UserPrincipal::de)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    // Usado pelo filtro: busco o usuário no banco a cada requisição para pegar sempre
    // o perfil atual (se o ADMIN mudou o perfil ou apagou o usuário, o token antigo perde o efeito).
    public UserPrincipal carregarPorId(Long id) {
        return userRepository.findById(id)
                .map(UserPrincipal::de)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
