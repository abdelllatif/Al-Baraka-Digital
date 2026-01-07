package com.albaraka.controllers;

import com.albaraka.config.CustomUserDetails;
import com.albaraka.dto.OperationRequest;
import com.albaraka.models.Document;
import com.albaraka.models.Operation;
import com.albaraka.models.User;
import com.albaraka.services.interfaces.OperationService;
import com.albaraka.services.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web")
public class WebController {
    private final UserService userService;
    private final OperationService operationService;

    @Autowired
    public WebController(UserService userService, OperationService operationService) {
        this.userService = userService;
        this.operationService = operationService;
    }
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }




    @GetMapping("/agent/dashboard")
    public String agentDashboard() {
        return "agent/dashboard";
    }


    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {

        List<User> users = userService.getAllUsers();

        long clients = users.stream()
                .filter(u -> "CLIENT".equals(u.getRole()))
                .count();

        long agents = users.stream()
                .filter(u -> "AGENT".equals(u.getRole()))
                .count();

        long admins = users.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .count();
        model.addAttribute("user", new User());

        model.addAttribute("users", users);
        model.addAttribute("clientsCount", clients);
        model.addAttribute("agentsCount", agents);
        model.addAttribute("adminsCount", admins);

        return "admin/dashboard";
    }


    // حفظ مستخدم جديد أو تحديث مستخدم
    @PostMapping("/admin/users/save")
    public String saveUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "admin/dashboard";
        }

        if (user.getId() == null) {
            userService.createUser(user);
        } else {
            userService.updateUser(user.getId(), user);
        }

        return "redirect:/web/admin/dashboard";
    }

    @PostMapping("/admin/users/update/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute User user,
            RedirectAttributes redirectAttributes
    ) {
        userService.updateUser(id, user);

        redirectAttributes.addFlashAttribute(
                "success",
                "User updated successfully ✅"
        );

        return "redirect:/web/admin/dashboard";
    }


    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        userService.deleteUser(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "User deleted successfully 🗑️"
        );

        return "redirect:/web/admin/dashboard";
    }




    @GetMapping("/client/dashboard")
    public String clientDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
        Long clientId = userService.findByEmail(customUserDetails.getUsername()).getId();

        List<Operation> operations = operationService.getClientOperations(clientId);
        model.addAttribute("operations", operations);
        model.addAttribute("newOperation", new OperationRequest()); // form backing object
        return "client/dashboard";
    }

    // ===== Operations =====
    @PostMapping("/client/operations/create")
    public String createOperation(
            @Valid @ModelAttribute("newOperation") OperationRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid operation data ❌");
            return "redirect:/web/client/dashboard";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
        Long clientId = userService.findByEmail(customUserDetails.getUsername()).getId();

        Operation operation = operationService.createOperation(clientId, request);

        redirectAttributes.addFlashAttribute("success", "Operation created successfully ✅");
        return "redirect:/web/client/dashboard";
    }

    @PostMapping("/client/operations/{id}/document")
    public String uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
        Long clientId = userService.findByEmail(customUserDetails.getUsername()).getId();

        try {
            Document document = operationService.uploadDocument(id, file, clientId);
            redirectAttributes.addFlashAttribute("success",
                    "Document '" + document.getFileName() + "' uploaded successfully ✅");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to upload document ❌: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard";
    }

    @GetMapping("/client/operations")
    public String listClientOperations(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
        Long clientId = userService.findByEmail(customUserDetails.getUsername()).getId();

        List<Operation> operations = operationService.getClientOperations(clientId);
        model.addAttribute("operations", operations);

        return "client/operations"; // you can create a separate Thymeleaf page if needed
    }
}