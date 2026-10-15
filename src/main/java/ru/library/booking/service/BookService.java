package ru.library.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.library.booking.dto.BookForm;
import ru.library.booking.exception.BusinessException;
import ru.library.booking.exception.NotFoundException;
import ru.library.booking.model.Book;
import ru.library.booking.model.ReservationStatus;
import ru.library.booking.repository.BookRepository;
import ru.library.booking.repository.ReservationRepository;
import ru.library.booking.storage.FileStorage;
import ru.library.booking.storage.StoredFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** CRUD для книг. */
@Service
public class BookService {

    private final BookRepository books;
    private final ReservationRepository reservations;
    private final FileStorage storage;

    public BookService(BookRepository books, ReservationRepository reservations, FileStorage storage) {
        this.books = books;
        this.reservations = reservations;
        this.storage = storage;
    }

    // ---------- READ ----------

    public List<Book> search(String query, String genre) {
        return books.search(query == null ? "" : query.trim(), genre == null ? "" : genre.trim());
    }

    public Book get(Long id) {
        return books.findById(id).orElseThrow(() -> new NotFoundException("Книга не найдена"));
    }

    public List<String> genres() {
        return books.findAllGenres();
    }

    public int available(Book book) {
        long taken = reservations.countByBookAndStatusIn(book, ReservationStatus.OCCUPYING);
        return (int) Math.max(0, book.getTotalCopies() - taken);
    }

    public Map<Long, Integer> availability(List<Book> list) {
        Map<Long, Integer> map = new LinkedHashMap<>();
        list.forEach(b -> map.put(b.getId(), available(b)));
        return map;
    }

    public StoredFile cover(Long id) {
        Book book = get(id);
        if (book.getCoverKey() == null) {
            throw new NotFoundException("У книги нет обложки");
        }
        return storage.load(book.getCoverKey());
    }

    // ---------- CREATE ----------

    @Transactional
    public Book create(BookForm form, MultipartFile cover) {
        Book book = new Book();
        form.applyTo(book);
        if (hasFile(cover)) {
            book.setCoverKey(storage.save(cover));
        }
        return books.save(book);
    }

    // ---------- UPDATE ----------

    @Transactional
    public Book update(Long id, BookForm form, MultipartFile cover, boolean removeCover) {
        Book book = get(id);
        long taken = reservations.countByBookAndStatusIn(book, ReservationStatus.OCCUPYING);
        if (form.getTotalCopies() < taken) {
            throw new BusinessException("Нельзя уменьшить число экземпляров ниже " + taken
                    + " — столько сейчас забронировано или выдано");
        }
        form.applyTo(book);
        if (hasFile(cover)) {
            String old = book.getCoverKey();
            book.setCoverKey(storage.save(cover));
            if (old != null) {
                storage.delete(old);
            }
        } else if (removeCover && book.getCoverKey() != null) {
            storage.delete(book.getCoverKey());
            book.setCoverKey(null);
        }
        return book;
    }

    // ---------- DELETE ----------

    @Transactional
    public void delete(Long id) {
        Book book = get(id);
        if (reservations.countByBookAndStatusIn(book, ReservationStatus.OCCUPYING) > 0) {
            throw new BusinessException("Книгу нельзя удалить: есть активные брони или выдачи");
        }
        reservations.deleteByBook(book);
        if (book.getCoverKey() != null) {
            storage.delete(book.getCoverKey());
        }
        books.delete(book);
    }

    private boolean hasFile(MultipartFile f) {
        return f != null && !f.isEmpty();
    }
}
