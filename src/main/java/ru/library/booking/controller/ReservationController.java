package ru.library.booking.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.library.booking.exception.BusinessException;
import ru.library.booking.model.Reservation;
import ru.library.booking.model.ReservationStatus;
import ru.library.booking.model.User;
import ru.library.booking.service.ReservationService;

import java.time.LocalDate;

/** CRUD броней. Читатель видит и меняет только свои, библиотекарь — все. */
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal User user,
                       @RequestParam(name = "status", required = false) ReservationStatus status,
                       Model model) {
        model.addAttribute("reservations", reservationService.listFor(user, status));
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("status", status);
        return "reservations/list";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal User user,
                         @RequestParam(name = "bookId") Long bookId,
                         @RequestParam(name = "days", defaultValue = "14") int days,
                         @RequestParam(name = "comment", required = false) String comment,
                         RedirectAttributes ra) {
        try {
            Reservation r = reservationService.create(user, bookId, days, comment);
            ra.addFlashAttribute("success", "Книга забронирована до " + r.getDueDate());
            return "redirect:/reservations";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/books/" + bookId;
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, @AuthenticationPrincipal User user, Model model) {
        model.addAttribute("r", reservationService.getFor(id, user));
        model.addAttribute("statuses", ReservationStatus.values());
        return "reservations/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @AuthenticationPrincipal User user,
                         @RequestParam(name = "dueDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                         @RequestParam(name = "comment", required = false) String comment,
                         @RequestParam(name = "status", required = false) ReservationStatus status,
                         RedirectAttributes ra) {
        try {
            reservationService.update(id, user, dueDate, comment, status);
            ra.addFlashAttribute("success", "Бронь обновлена");
            return "redirect:/reservations";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservations/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long id, @AuthenticationPrincipal User user, RedirectAttributes ra) {
        try {
            reservationService.cancel(id, user);
            ra.addFlashAttribute("success", "Бронь отменена");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, @AuthenticationPrincipal User user, RedirectAttributes ra) {
        try {
            reservationService.delete(id, user);
            ra.addFlashAttribute("success", "Бронь удалена");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }
}
