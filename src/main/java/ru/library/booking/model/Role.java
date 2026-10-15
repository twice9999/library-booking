package ru.library.booking.model;

/** Роли пользователей системы. */
public enum Role {
    CLIENT("Читатель"),
    ADMIN("Библиотекарь");

    private final String title;

    Role(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
