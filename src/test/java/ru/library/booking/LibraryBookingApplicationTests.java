package ru.library.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.library.booking.exception.BusinessException;
import ru.library.booking.model.Book;
import ru.library.booking.model.ReservationStatus;
import ru.library.booking.model.User;
import ru.library.booking.repository.BookRepository;
import ru.library.booking.service.ReservationService;
import ru.library.booking.service.UserService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.storage.local-dir=target/test-data")
@AutoConfigureMockMvc
@Transactional
class LibraryBookingApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired UserService userService;
    @Autowired BookRepository books;
    @Autowired ReservationService reservations;

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/books")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void clientSeesCatalog() throws Exception {
        User client = userService.loadUserByUsername("client");
        mvc.perform(get("/books").with(user(client)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Мастер и Маргарита")));
    }

    @Test
    void clientCannotCreateBook() throws Exception {
        User client = userService.loadUserByUsername("client");
        mvc.perform(get("/books/new").with(user(client))).andExpect(status().isForbidden());
        mvc.perform(post("/books").with(user(client)).with(csrf())
                        .param("title", "X").param("author", "Y").param("totalCopies", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesBookWithCover() throws Exception {
        User admin = userService.loadUserByUsername("admin");
        var cover = new MockMultipartFile("cover", "cover.png", "image/png", new byte[]{1, 2, 3});
        mvc.perform(multipart("/books").file(cover).with(user(admin)).with(csrf())
                        .param("title", "Тестовая книга").param("author", "Автор").param("totalCopies", "2"))
                .andExpect(status().is3xxRedirection());

        Book saved = books.search("Тестовая", "").get(0);
        assertThat(saved.getCoverKey()).startsWith("covers/");
        mvc.perform(get("/books/" + saved.getId() + "/cover")).andExpect(status().isOk());
    }

    @Test
    void reservationLifecycle() {
        User client = userService.loadUserByUsername("client");
        User admin = userService.loadUserByUsername("admin");
        Book book = books.save(new Book("Единственная", "Автор", "Тест", 2020, null, 1));

        var r = reservations.create(client, book.getId(), 7, "заберу завтра");
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(r.getDueDate()).isEqualTo(LocalDate.now().plusDays(7));

        // экземпляр один — повторно забронировать нельзя
        assertThatThrownBy(() -> reservations.create(admin, book.getId(), 7, null))
                .isInstanceOf(BusinessException.class);

        reservations.update(r.getId(), admin, null, null, ReservationStatus.ISSUED);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.ISSUED);

        // выданную бронь нельзя отменить
        assertThatThrownBy(() -> reservations.cancel(r.getId(), client))
                .isInstanceOf(BusinessException.class);

        reservations.update(r.getId(), admin, null, null, ReservationStatus.RETURNED);
        reservations.delete(r.getId(), client);
        assertThat(reservations.listFor(client, null)).noneMatch(x -> x.getId().equals(r.getId()));
    }

    @Test
    void clientCannotExtendBeyondLimit() {
        User client = userService.loadUserByUsername("client");
        Book book = books.save(new Book("Лимит", "Автор", "Тест", 2020, null, 1));
        var r = reservations.create(client, book.getId(), 7, null);
        assertThatThrownBy(() -> reservations.update(r.getId(), client, LocalDate.now().plusDays(60), null, null))
                .isInstanceOf(BusinessException.class);
    }
}
