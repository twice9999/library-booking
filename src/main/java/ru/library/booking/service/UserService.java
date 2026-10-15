package ru.library.booking.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.library.booking.dto.RegistrationForm;
import ru.library.booking.exception.BusinessException;
import ru.library.booking.model.Role;
import ru.library.booking.model.User;
import ru.library.booking.repository.UserRepository;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public User loadUserByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    /** Регистрация нового читателя (роль CLIENT). */
    @Transactional
    public User register(RegistrationForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Пароли не совпадают");
        }
        if (users.existsByUsername(form.getUsername())) {
            throw new BusinessException("Логин уже занят");
        }
        User user = new User(form.getUsername(), encoder.encode(form.getPassword()),
                form.getFullName().trim(), Role.CLIENT);
        return users.save(user);
    }

    @Transactional
    public User createIfMissing(String username, String rawPassword, String fullName, Role role) {
        return users.findByUsername(username)
                .orElseGet(() -> users.save(new User(username, encoder.encode(rawPassword), fullName, role)));
    }

    public List<User> findAll() {
        return users.findAllByOrderByCreatedAtDesc();
    }
}
