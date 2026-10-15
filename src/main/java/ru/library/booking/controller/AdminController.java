package ru.library.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.library.booking.model.User;
import ru.library.booking.service.ReservationService;
import ru.library.booking.service.UserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ReservationService reservationService;

    public AdminController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = userService.findAll();
        Map<Long, Long> counts = new LinkedHashMap<>();
        users.forEach(u -> counts.put(u.getId(), reservationService.countByUser(u)));
        model.addAttribute("users", users);
        model.addAttribute("counts", counts);
        return "admin/users";
    }
}
