package ru.library.booking.storage;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.library.booking.exception.BusinessException;

import java.util.Set;
import java.util.UUID;

final class StorageKeys {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private StorageKeys() {
    }

    /** Проверяет, что загружена картинка, и генерирует уникальный ключ. */
    static String newKey(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        ext = ext == null ? "" : ext.toLowerCase();
        if (!ALLOWED.contains(ext)) {
            throw new BusinessException("Обложка должна быть изображением: jpg, png, gif или webp");
        }
        return "covers/" + UUID.randomUUID() + "." + ext;
    }

    static String contentTypeByKey(String key) {
        String ext = StringUtils.getFilenameExtension(key);
        if (ext == null) {
            return "application/octet-stream";
        }
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
