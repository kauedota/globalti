package br.com.globalti.prova.config;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Essa classe serve para criar o usuário ADMIN inicial quando a API sobe.
 * O email e a senha vêm da configuração (veja o README). Se o admin já existe, não faz nada.
 * Faço por aqui (e não por migration) para o hash da senha ser gerado na hora, sem ficar no repositório.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String email;
    private final String senha;

    public AdminInicialRunner(UserRepository userRepository, PasswordEncoder passwordEncoder,
                              @Value("${app.admin.nome}") String nome,
                              @Value("${app.admin.email}") String email,
                              @Value("${app.admin.password}") String senha) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.email = email.trim().toLowerCase();
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = new User();
        admin.setNome(nome);
        admin.setEmail(email);
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Usuário ADMIN inicial criado: {}", email);
    }
}
