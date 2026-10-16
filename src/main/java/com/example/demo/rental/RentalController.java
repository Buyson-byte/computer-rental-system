
package com.example.rental;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RentalController {

    private static final double RATE = 20.00;
    private final List<Computer> computers = new ArrayList<>();

    public RentalController() {
        for (int i = 1; i <= 10; i++) {
            computers.add(new Computer(i));
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        for (Computer pc : computers) {
            releaseIfExpired(pc);
        }

        model.addAttribute("computers", computers);
        model.addAttribute("rate", RATE);
        return "index";
    }

    @PostMapping("/rent")
    public String rent(
            @RequestParam int computerId,
            @RequestParam String customerName,
            @RequestParam int hours,
            @RequestParam double payment,
            RedirectAttributes redirect) {

        if (computerId < 1 || computerId > computers.size()) {
            redirect.addFlashAttribute("message",
                    "Invalid computer number.");
            return "redirect:/";
        }

        Computer pc = computers.get(computerId - 1);
        releaseIfExpired(pc);

        if (customerName == null || customerName.isBlank()
                || hours < 1 || hours > 720
                || !Double.isFinite(payment) || payment < 0) {
            redirect.addFlashAttribute("message",
                    "Please enter valid rental details.");
        } else if (!pc.available) {
            redirect.addFlashAttribute("message",
                    "This computer is already occupied.");
        } else {
            double fee = hours * RATE;

            if (payment < fee) {
                redirect.addFlashAttribute("message",
                        "Insufficient payment. Rental fee: PHP "
                                + String.format("%.2f", fee));
            } else {
                pc.available = false;
                pc.customer = customerName.trim();
                pc.paid = fee;
                pc.hours = hours;
                pc.startTime = System.currentTimeMillis();
                pc.endTime = pc.startTime + hours * 60L * 60L * 1000L;

                redirect.addFlashAttribute("message",
                        "Rental successful! Change: PHP "
                                + String.format("%.2f", payment - fee));
                redirect.addFlashAttribute("change", payment - fee);
                redirect.addFlashAttribute("successComputerId", pc.id);
                redirect.addFlashAttribute("view", "confirmation");
            }
        }

        return "redirect:/";
    }

    @PostMapping("/extend")
    public String extend(
            @RequestParam int computerId,
            @RequestParam int minutes,
            @RequestParam double payment,
            RedirectAttributes redirect) {

        if (computerId < 1 || computerId > computers.size()
                || minutes < 30 || minutes > 43_200 || minutes % 30 != 0
                || !Double.isFinite(payment) || payment < 0) {
            redirect.addFlashAttribute("message", "Please enter valid extension details.");
            redirect.addFlashAttribute("view", "extend");
            return "redirect:/";
        }

        Computer pc = computers.get(computerId - 1);
        releaseIfExpired(pc);
        if (pc.available) {
            redirect.addFlashAttribute("message", "That computer has no active rental to extend.");
            redirect.addFlashAttribute("view", "extend");
        } else {
            double hours = minutes / 60.0;
            double fee = hours * RATE;
            if (payment < fee) {
                redirect.addFlashAttribute("message", "Insufficient payment. Extension fee: PHP "
                        + String.format("%.2f", fee));
                redirect.addFlashAttribute("view", "extend");
            } else {
                long extensionMillis = minutes * 60_000L;
                double change = payment - fee;
                pc.endTime += extensionMillis;
                pc.hours += hours;
                pc.paid += fee;
                redirect.addFlashAttribute("message", "Extension paid: PHP "
                        + String.format("%.2f", fee) + ". Change: PHP "
                        + String.format("%.2f", change));
                redirect.addFlashAttribute("view", "remaining");
            }
        }
        return "redirect:/";
    }

    @PostMapping("/end")
    public String end(
            @RequestParam int computerId,
            RedirectAttributes redirect) {

        if (computerId < 1 || computerId > computers.size()) {
            redirect.addFlashAttribute("message", "Invalid computer number.");
            redirect.addFlashAttribute("view", "end");
            return "redirect:/";
        }

        Computer pc = computers.get(computerId - 1);
        if (pc.available) {
            redirect.addFlashAttribute("message", "That computer has no active rental.");
            redirect.addFlashAttribute("view", "end");
        } else {
            clearRental(pc);
            redirect.addFlashAttribute("message", "Session ended. The computer is available again.");
            redirect.addFlashAttribute("view", "menu");
        }
        return "redirect:/";
    }

    private void releaseIfExpired(Computer pc) {
        if (!pc.available
                && System.currentTimeMillis() >= pc.endTime) {
            clearRental(pc);
        }
    }

    private void clearRental(Computer pc) {
        pc.available = true;
        pc.customer = "";
        pc.paid = 0;
        pc.hours = 0;
        pc.startTime = 0;
        pc.endTime = 0;
    }

    public double getRate() {
        return RATE;
    }

    public List<Computer> getComputers() {
        return computers;
    }

    public static String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }

    public static class Computer {
        public int id;
        public boolean available = true;
        public String customer = "";
        public double paid;
        public double hours;
        public long startTime;
        public long endTime;

        public Computer(int id) {
            this.id = id;
        }
    }
}
