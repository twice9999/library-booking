# 📚 Читальня — онлайн-бронирование книг в библиотеке

Веб-приложение, в котором читатели находят книги в каталоге и бронируют их, а библиотекарь ведёт каталог и управляет выдачей.

**Стек:** Java 17, Spring Boot 3 (Web, Security, Data JPA, Validation), Thymeleaf, PostgreSQL / H2, MinIO (S3), Docker Compose, JUnit 5.

Демо-аккаунты (создаются автоматически):

| Логин  | Пароль | Роль                    |
|--------|--------|-------------------------|
| admin  | admin  | Библиотекарь (ADMIN)    |
| client | client | Читатель (CLIENT)       |

## Вариант 1. Запуск из IntelliJ IDEA (без Docker)

1. **File → Open** → выбрать папку проекта. IDEA сама подтянет зависимости Maven.
2. Если IDEA попросит JDK — выбрать любую версию от 17 (можно скачать прямо из окна: *Download JDK*).
3. Запустить `LibraryBookingApplication` (зелёный треугольник рядом с `main` или готовая конфигурация вверху справа).
4. Открыть http://localhost:8080

В этом режиме используется встроенная база H2 (данные живут, пока запущено приложение), а обложки хранятся в папке `./data`.

## Вариант 2. Полный стенд в Docker (сервер + БД + S3)

```bash
docker compose up --build
```

| Сервис        | Адрес                  | Что это                                  |
|---------------|------------------------|------------------------------------------|
| app           | http://localhost:8080  | Сервер и веб-интерфейс                   |
| db            | localhost:5432         | PostgreSQL (library / library)           |
| minio         | http://localhost:9001  | Консоль S3-хранилища (minioadmin / minioadmin) |

Остановить: `docker compose down` (с удалением данных: `docker compose down -v`).

## Тесты

```bash
mvn test
```

## Структура

```
src/main/java/ru/library/booking
├── config/        безопасность (роли CLIENT/ADMIN), демо-данные
├── controller/    веб-контроллеры (книги, брони, вход, админка)
├── dto/           формы с валидацией
├── exception/     бизнес-ошибки
├── model/         сущности: User, Book, Reservation
├── repository/    Spring Data JPA
├── service/       бизнес-логика и CRUD
└── storage/       интеграция с S3 (MinIO) + локальное хранилище
docs/              описание проекта и 60 use cases
```

Подробности — в [docs/Описание_проекта.docx](docs/Описание_проекта.docx) и [docs/Use_cases.docx](docs/Use_cases.docx).
