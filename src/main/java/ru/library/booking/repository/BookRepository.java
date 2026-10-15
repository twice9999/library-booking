package ru.library.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.library.booking.model.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    /** Поиск по названию/автору и фильтр по жанру. Пустая строка означает «без фильтра». */
    @Query("""
            select b from Book b
            where (lower(b.title) like lower(concat('%', :q, '%'))
                   or lower(b.author) like lower(concat('%', :q, '%')))
              and (:genre = '' or b.genre = :genre)
            order by b.title
            """)
    List<Book> search(@Param("q") String q, @Param("genre") String genre);

    @Query("select distinct b.genre from Book b where b.genre is not null and b.genre <> '' order by b.genre")
    List<String> findAllGenres();
}
