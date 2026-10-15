package ru.library.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.library.booking.exception.BusinessException;
import ru.library.booking.exception.ForbiddenException;
import ru.library.booking.exception.NotFoundException;
import ru.library.booking.model.*;
import ru.library.booking.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;

/** CRUD для броней с бизнес-правилами. */
@Service
public class ReservationService {

    public static final int MAX_ACTIVE_PER_USER = 5;
    public static final int MAX_DAYS = 30;

    private final ReservationRepository reservations;
    private final BookService bookService;

    public ReservationService(ReservationRepository reservations, BookService bookService) {
        this.reservations = reservations;
        this.bookService = bookService;
    }

    // ---------- CREATE ----------

    @Transactional
    public Reservation create(User user, Long bookId, int days, String comment) {
        if (days < 1 || days > MAX_DAYS) {
            throw new BusinessException("Срок брони — от 1 до " + MAX_DAYS + " дней");
        }
        Book book = bookService.get(bookId);
        if (bookService.available(book) <= 0) {
            throw new BusinessException("Свободных экземпляров нет");
        }
        if (reservations.existsByUserAndBookAndStatusIn(user, book, ReservationStatus.OCCUPYING)) {
            throw new BusinessException("У вас уже есть активная бронь этой книги");
        }
        if (reservations.countByUserAndStatusIn(user, ReservationStatus.OCCUPYING) >= MAX_ACTIVE_PER_USER) {
            throw new BusinessException("Нельзя иметь больше " + MAX_ACTIVE_PER_USER + " активных броней");
        }
        Reservation r = new Reservation(user, book, LocalDate.now().plusDays(days), trim(comment));
        return reservations.save(r);
    }

    // ---------- READ ----------

    public List<Reservation> listFor(User user, ReservationStatus status) {
        if (user.isAdmin()) {
            return status == null
                    ? reservations.findAllByOrderByCreatedAtDesc()
                    : reservations.findByStatusOrderByCreatedAtDesc(status);
        }
        List<Reservation> own = reservations.findByUserOrderByCreatedAtDesc(user);
        return status == null ? own : own.stream().filter(r -> r.getStatus() == status).toList();
    }

    public Reservation getFor(Long id, User user) {
        Reservation r = reservations.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));
        if (!user.isAdmin() && !r.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Это чужая бронь");
        }
        return r;
    }

    public boolean hasOpenReservation(User user, Book book) {
        return reservations.existsByUserAndBookAndStatusIn(user, book, ReservationStatus.OCCUPYING);
    }

    public long countByUser(User user) {
        return reservations.countByUser(user);
    }

    // ---------- UPDATE ----------

    /**
     * Читатель может изменить срок и комментарий своей открытой брони.
     * Библиотекарь дополнительно меняет статус (выдать / принять возврат / отменить).
     */
    @Transactional
    public Reservation update(Long id, User actor, LocalDate dueDate, String comment, ReservationStatus newStatus) {
        Reservation r = getFor(id, actor);
        if (!actor.isAdmin() && !r.getStatus().isOpen()) {
            throw new BusinessException("Закрытую бронь изменить нельзя");
        }
        if (dueDate != null) {
            LocalDate max = r.getCreatedAt().toLocalDate().plusDays(MAX_DAYS);
            if (dueDate.isBefore(LocalDate.now()) && r.getStatus().isOpen() && !actor.isAdmin()) {
                throw new BusinessException("Дата не может быть в прошлом");
            }
            if (dueDate.isAfter(max) && !actor.isAdmin()) {
                throw new BusinessException("Максимальный срок — " + MAX_DAYS + " дней с даты брони (до " + max + ")");
            }
            r.setDueDate(dueDate);
        }
        r.setComment(trim(comment));
        if (actor.isAdmin() && newStatus != null && newStatus != r.getStatus()) {
            changeStatus(r, newStatus);
        }
        return r;
    }

    @Transactional
    public void cancel(Long id, User actor) {
        Reservation r = getFor(id, actor);
        if (r.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException("Отменить можно только бронь в статусе «Забронирована»");
        }
        r.setStatus(ReservationStatus.CANCELLED);
    }

    // ---------- DELETE ----------

    @Transactional
    public void delete(Long id, User actor) {
        Reservation r = getFor(id, actor);
        if (!actor.isAdmin() && r.getStatus().isOpen()) {
            throw new BusinessException("Сначала отмените бронь, потом её можно удалить из истории");
        }
        if (r.getStatus() == ReservationStatus.ISSUED) {
            throw new BusinessException("Книга на руках у читателя — сначала примите возврат");
        }
        reservations.delete(r);
    }

    private void changeStatus(Reservation r, ReservationStatus to) {
        ReservationStatus from = r.getStatus();
        boolean ok = switch (from) {
            case ACTIVE -> to == ReservationStatus.ISSUED || to == ReservationStatus.CANCELLED;
            case ISSUED -> to == ReservationStatus.RETURNED;
            case RETURNED, CANCELLED -> false;
        };
        if (!ok) {
            throw new BusinessException("Нельзя перевести бронь из «" + from.getTitle() + "» в «" + to.getTitle() + "»");
        }
        r.setStatus(to);
    }

    private String trim(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
