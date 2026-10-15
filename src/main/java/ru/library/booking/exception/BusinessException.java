package ru.library.booking.exception;

/** Нарушение бизнес-правила — сообщение показывается пользователю. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
