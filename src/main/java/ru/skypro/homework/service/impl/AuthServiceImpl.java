package ru.skypro.homework.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {


    private final PasswordEncoder encoder;
    private final UserRepository repository;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.encoder = passwordEncoder;
        this.repository = repository;
    }

    @Override
    public boolean login(String userName, String password) {

        Optional<UserEntity> entity = repository.findByEmail(userName);

        if (entity.isEmpty()) return false;

        return encoder.matches(password, entity.get().getPassword());
    }
}
