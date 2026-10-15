package ru.library.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.library.booking.model.Book;
import ru.library.booking.model.Reservation;
import ru.library.booking.model.ReservationStatus;
import ru.library.booking.model.User;

import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserOrderByCreatedAtDesc(User user);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    List<Reservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status);

    long countByBookAndStatusIn(Book book, Collection<ReservationStatus> statuses);

    long countByUserAndStatusIn(User user, Collection<ReservationStatus> statuses);

    long countByUser(User user);

    boolean existsByUserAndBookAndStatusIn(User user, Book book, Collection<ReservationStatus> statuses);

    void deleteByBook(Book book);
}
