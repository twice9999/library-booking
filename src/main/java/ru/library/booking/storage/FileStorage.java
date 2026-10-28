package ru.library.booking.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Хранилище файлов по принципу key-value.
 * В Docker используется MinIO (S3), при запуске из IDE - локальная папка.
 */
public interface FileStorage {

    /** Сохраняет файл и возвращает его ключ. */
    String save(MultipartFile file);

    /** Загружает файл по ключу. */
    StoredFile load(String key);

    /** Удаляет файл по ключу (если существует). */
    void delete(String key);
}
