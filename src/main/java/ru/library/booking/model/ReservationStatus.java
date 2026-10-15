package ru.library.booking.model;

import java.util.EnumSet;
import java.util.Set;

/** Жизненный цикл брони. */
public enum ReservationStatus {
    ACTIVE("Забронирована"),
    ISSUED("Выдана"),
    RETURNED("Возвращена"),
    CANCELLED("Отменена");

    /** Статусы, при которых экземпляр книги занят. */
    public static final Set<ReservationStatus> OCCUPYING = EnumSet.of(ACTIVE, ISSUED);

    private final String title;

    ReservationStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isOpen() {
        return OCCUPYING.contains(this);
    }
}
