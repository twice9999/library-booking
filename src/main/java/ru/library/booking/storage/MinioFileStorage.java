package ru.library.booking.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.library.booking.exception.NotFoundException;

import java.io.InputStream;

/** Интеграция с S3-совместимым хранилищем MinIO: обложки книг хранятся как key -> value. */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "minio")
public class MinioFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(MinioFileStorage.class);

    private final MinioClient client;
    private final String bucket;
    private volatile boolean bucketReady;

    public MinioFileStorage(@Value("${app.minio.endpoint}") String endpoint,
                            @Value("${app.minio.access-key}") String accessKey,
                            @Value("${app.minio.secret-key}") String secretKey,
                            @Value("${app.minio.bucket}") String bucket) {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
        try {
            ensureBucket();
        } catch (Exception e) {
            log.warn("MinIO пока недоступен, бакет будет создан при первой загрузке: {}", e.getMessage());
        }
    }

    @Override
    public String save(MultipartFile file) {
        String key = StorageKeys.newKey(file);
        try (InputStream in = file.getInputStream()) {
            ensureBucket();
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, file.getSize(), -1)
                    .contentType(StorageKeys.contentTypeByKey(key))
                    .build());
            return key;
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить файл в MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public StoredFile load(String key) {
        try {
            ensureBucket();
            try (InputStream in = client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build())) {
                return new StoredFile(in.readAllBytes(), StorageKeys.contentTypeByKey(key));
            }
        } catch (ErrorResponseException e) {
            throw new NotFoundException("Файл не найден");
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка чтения из MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            log.warn("Не удалось удалить {} из MinIO: {}", key, e.getMessage());
        }
    }

    private void ensureBucket() throws Exception {
        if (bucketReady) {
            return;
        }
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("Создан бакет MinIO '{}'", bucket);
        }
        bucketReady = true;
    }
}
