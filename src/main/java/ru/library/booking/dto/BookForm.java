package ru.library.booking.dto;

import jakarta.validation.constraints.*;
import ru.library.booking.model.Book;

public class BookForm {

    @NotBlank(message = "Укажите название")
    @Size(max = 200, message = "Не длиннее 200 символов")
    private String title;

    @NotBlank(message = "Укажите автора")
    @Size(max = 150, message = "Не длиннее 150 символов")
    private String author;

    @Size(max = 60, message = "Не длиннее 60 символов")
    private String genre;

    @Min(value = 1000, message = "Некорректный год")
    @Max(value = 2100, message = "Некорректный год")
    private Integer publishYear;

    @Size(max = 2000, message = "Не длиннее 2000 символов")
    private String description;

    @NotNull(message = "Укажите количество экземпляров")
    @Min(value = 0, message = "Не может быть отрицательным")
    @Max(value = 1000, message = "Не больше 1000")
    private Integer totalCopies = 1;

    public static BookForm from(Book b) {
        BookForm f = new BookForm();
        f.title = b.getTitle();
        f.author = b.getAuthor();
        f.genre = b.getGenre();
        f.publishYear = b.getPublishYear();
        f.description = b.getDescription();
        f.totalCopies = b.getTotalCopies();
        return f;
    }

    public void applyTo(Book b) {
        b.setTitle(title.trim());
        b.setAuthor(author.trim());
        b.setGenre(genre == null || genre.isBlank() ? null : genre.trim());
        b.setPublishYear(publishYear);
        b.setDescription(description);
        b.setTotalCopies(totalCopies);
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
}
