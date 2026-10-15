package ru.library.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.library.booking.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAllByOrderByCreatedAtDesc();
}
