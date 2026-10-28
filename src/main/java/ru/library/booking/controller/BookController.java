package ru.library.booking.controller;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.library.booking.dto.BookForm;
import ru.library.booking.exception.BusinessException;
import ru.library.booking.model.Book;
import ru.library.booking.model.User;
import ru.library.booking.service.BookService;
import ru.library.booking.service.ReservationService;
import ru.library.booking.storage.StoredFile;

import java.time.Duration;
import java.util.List;

/** CRUD книг: читать могут все, изменять - только библиотекарь (см. SecurityConfig). */
@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final ReservationService reservationService;

    public BookController(BookService bookService, ReservationService reservationService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "genre", required = false) String genre,
                       @RequestParam(name = "onlyAvailable", required = false, defaultValue = "false") boolean onlyAvailable,
                       Model model) {
        List<Book> books = bookService.search(q, genre);
        var availability = bookService.availability(books);
        if (onlyAvailable) {
            books = books.stream().filter(b -> availability.get(b.getId()) > 0).toList();
        }
        model.addAttribute("books", books);
        model.addAttribute("availability", availability);
        model.addAttribute("genres", bookService.genres());
        model.addAttribute("q", q);
        model.addAttribute("genre", genre);
        model.addAttribute("onlyAvailable", onlyAvailable);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable("id") Long id, @AuthenticationPrincipal User user, Model model) {
        Book book = bookService.get(id);
        model.addAttribute("book", book);
        model.addAttribute("available", bookService.available(book));
        model.addAttribute("hasReservation", reservationService.hasOpenReservation(user, book));
        model.addAttribute("maxDays", ReservationService.MAX_DAYS);
        return "books/view";
    }

    @GetMapping("/{id}/cover")
    public ResponseEntity<byte[]> cover(@PathVariable("id") Long id) {
        StoredFile file = bookService.cover(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(file.data());
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        model.addAttribute("book", null);
        return "books/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookForm form, BindingResult result,
                         @RequestParam(name = "cover", required = false) MultipartFile cover,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("book", null);
            return "books/form";
        }
        try {
            Book book = bookService.create(form, cover);
            ra.addFlashAttribute("success", "Книга «" + book.getTitle() + "» добавлена");
            return "redirect:/books/" + book.getId();
        } catch (BusinessException e) {
            model.addAttribute("book", null);
            model.addAttribute("error", e.getMessage());
            return "books/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.get(id);
        model.addAttribute("form", BookForm.from(book));
        model.addAttribute("book", book);
        return "books/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("form") BookForm form, BindingResult result,
                         @RequestParam(name = "cover", required = false) MultipartFile cover,
                         @RequestParam(name = "removeCover", required = false, defaultValue = "false") boolean removeCover,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("book", bookService.get(id));
            return "books/form";
        }
        try {
            bookService.update(id, form, cover, removeCover);
            ra.addFlashAttribute("success", "Изменения сохранены");
            return "redirect:/books/" + id;
        } catch (BusinessException e) {
            model.addAttribute("book", bookService.get(id));
            model.addAttribute("error", e.getMessage());
            return "books/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            bookService.delete(id);
            ra.addFlashAttribute("success", "Книга удалена");
            return "redirect:/books";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/books/" + id;
        }
    }
}
