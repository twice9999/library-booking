package ru.library.booking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Бизнес-сущность №1 - книга в каталоге библиотеки. */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(length = 60)
    private String genre;

    private Integer publishYear;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private int totalCopies;

    /** Ключ файла обложки в хранилище (S3 / MinIO). */
    @Column(length = 200)
    private String coverKey;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Book() {
    }

    public Book(String title, String author, String genre, Integer publishYear, String description, int totalCopies) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publishYear = publishYear;
        this.description = description;
        this.totalCopies = totalCopies;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public String getCoverKey() { return coverKey; }
    public void setCoverKey(String coverKey) { this.coverKey = coverKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
