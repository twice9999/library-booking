package ru.library.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.library.booking.exception.ForbiddenException;
import ru.library.booking.exception.NotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NotFoundException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Exception e, Model model) {
        model.addAttribute("code", 404);
        model.addAttribute("message", e instanceof NotFoundException ? e.getMessage() : "Страница не найдена");
        return "error";
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden(ForbiddenException e, Model model) {
        model.addAttribute("code", 403);
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String tooLarge(Model model) {
        model.addAttribute("code", 413);
        model.addAttribute("message", "Файл слишком большой (максимум 5 МБ)");
        return "error";
    }
}
