package ru.library.booking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.library.booking.model.Book;
import ru.library.booking.model.Role;
import ru.library.booking.repository.BookRepository;
import ru.library.booking.service.UserService;

import java.util.List;

/** Заполняет базу демонстрационными данными при первом запуске. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final BookRepository books;

    public DataInitializer(UserService userService, BookRepository books) {
        this.userService = userService;
        this.books = books;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userService.createIfMissing("admin", "admin", "Анна Библиотекарь", Role.ADMIN);
        userService.createIfMissing("client", "client", "Иван Читатель", Role.CLIENT);

        if (books.count() == 0) {
            books.saveAll(List.of(
                    new Book("Мастер и Маргарита", "Михаил Булгаков", "Роман", 1967,
                            "Роман о визите дьявола в Москву 1930-х и о любви Мастера и Маргариты.", 3),
                    new Book("Преступление и наказание", "Фёдор Достоевский", "Роман", 1866,
                            "История студента Раскольникова, решившегося на убийство ради идеи.", 2),
                    new Book("Пикник на обочине", "Аркадий и Борис Стругацкие", "Фантастика", 1972,
                            "Сталкеры пробираются в Зону, оставленную пришельцами.", 2),
                    new Book("Солярис", "Станислав Лем", "Фантастика", 1961,
                            "Учёные на станции у планеты-океана сталкиваются с непостижимым разумом.", 1),
                    new Book("Чистый код", "Роберт Мартин", "Программирование", 2008,
                            "Принципы написания понятного и поддерживаемого кода.", 4),
                    new Book("Грокаем алгоритмы", "Адитья Бхаргава", "Программирование", 2016,
                            "Иллюстрированное введение в алгоритмы.", 2),
                    new Book("Евгений Онегин", "Александр Пушкин", "Поэзия", 1833,
                            "Роман в стихах о судьбе молодого дворянина.", 3),
                    new Book("Краткая история времени", "Стивен Хокинг", "Наука", 1988,
                            "Популярно о Большом взрыве, чёрных дырах и природе времени.", 1)
            ));
            log.info("Добавлены демонстрационные книги");
        }
        log.info("Демо-аккаунты: admin/admin (библиотекарь), client/client (читатель)");
    }
}
