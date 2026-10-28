package ru.library.booking.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.library.booking.exception.NotFoundException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Хранение обложек в локальной папке - для запуска из IntelliJ IDEA без Docker. */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(@Value("${app.storage.local-dir:./data}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    @Override
    public String save(MultipartFile file) {
        String key = StorageKeys.newKey(file);
        try {
            Path target = resolve(key);
            Files.createDirectories(target.getParent());
            file.transferTo(target);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить файл", e);
        }
    }

    @Override
    public void put(String key, byte[] data) {
        try {
            Path target = resolve(key);
            Files.createDirectories(target.getParent());
            Files.write(target, data);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить файл", e);
        }
    }

    @Override
    public StoredFile load(String key) {
        Path path = resolve(key);
        if (!Files.exists(path)) {
            throw new NotFoundException("Файл не найден");
        }
        try {
            return new StoredFile(Files.readAllBytes(path), StorageKeys.contentTypeByKey(key));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {
            // удаление обложки не критично
        }
    }

    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new NotFoundException("Файл не найден");
        }
        return path;
    }
}
