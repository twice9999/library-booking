# 📚 Читальня - онлайн-бронирование книг в библиотеке

Веб-приложение, в котором читатели находят книги в каталоге и бронируют их, а библиотекарь ведёт каталог и управляет выдачей.

**Стек:** Java 17, Spring Boot 3 (Web, Security, Data JPA, Validation), Thymeleaf, PostgreSQL / H2, MinIO (S3), Docker Compose, JUnit 5.

Демо-аккаунты (создаются автоматически):

| Логин  | Пароль | Роль                    |
|--------|--------|-------------------------|
| admin  | admin  | Библиотекарь (ADMIN)    |
| client | client | Читатель (CLIENT)       |

## Вариант 1. Запуск из IntelliJ IDEA (без Docker)

1. **File → Open** → выбрать папку проекта. IDEA сама подтянет зависимости Maven.
2. Если IDEA попросит JDK - выбрать любую версию от 17 (можно скачать прямо из окна: *Download JDK*).
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
| db            | localhost:5433         | PostgreSQL (library / library)           |
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

## Документация

- [Описание проекта](docs/description.md): роли, CRUD, архитектура, интеграция с S3, модель данных
- [Use cases](docs/use-cases.md): 60 пользовательских сценариев, из них 20 MVP

## Соответствие требованиям задания

| № | Требование | Где выполнено |
|---|---|---|
| 1 | Git + описание проекта | Этот репозиторий, [docs/description.md](docs/description.md) |
| 2 | 60 use cases, 20 MVP | [docs/use-cases.md](docs/use-cases.md) |
| 3 | Сервер + БД + UI в Docker | [docker-compose.yml](docker-compose.yml), [Dockerfile](Dockerfile) |
| 4 | 2 CRUD по бизнес-сущностям | Книга ([BookController](src/main/java/ru/library/booking/controller/BookController.java)) и Бронь ([ReservationController](src/main/java/ru/library/booking/controller/ReservationController.java)) |
| 5 | Авторизация Client / Admin | [SecurityConfig](src/main/java/ru/library/booking/config/SecurityConfig.java) |
| +1 | Интеграция | S3 (MinIO): [MinioFileStorage](src/main/java/ru/library/booking/storage/MinioFileStorage.java) |
