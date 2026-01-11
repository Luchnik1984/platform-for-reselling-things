package ru.skypro.homework.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.reg.Register;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.RegService;

@Service
public class RegServiceImpl implements RegService {

    private final PasswordEncoder encoder;
    private final UserRepository repository;
    private final UserMapper mapper;

    public RegServiceImpl(PasswordEncoder encoder, UserRepository repository, UserMapper mapper) {
        this.encoder = encoder;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean register(Register register) {

        if (repository.existsByEmail(register.getUsername())) {
            return false;
        }

        UserEntity entity = mapper.toEntity(register);
        entity.setPassword(encoder.encode(register.getPassword()));

        repository.save(entity);

        return true;
    }

}
