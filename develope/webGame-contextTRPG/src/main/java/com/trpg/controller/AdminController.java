package com.trpg.controller;

import com.trpg.dto.admin.UserSummaryDTO;
import com.trpg.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminController {

    private static final int RECENT_USER_LIMIT = 10;

    private final AdminService adminService;

    // ════════════════════════════════════════════════════════════════════════
    // View endpoints
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/dashboard
     * Render the admin dashboard page (ADMIN only).
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        model.addAttribute("userCount", adminService.getUserCount());
        model.addAttribute("recentUsers", adminService.getRecentUsers(RECENT_USER_LIMIT));
        model.addAttribute("activePage", "admin");
        return "admin/dashboard";
    }

    /**
     * GET /admin/users
     * Render the user management page (ADMIN only).
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersPage(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("activePage", "admin");
        return "admin/users";
    }

    // ════════════════════════════════════════════════════════════════════════
    // REST API endpoints
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/admin/users
     * Return the full list of users as JSON.
     */
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * PUT /api/admin/users/{id}/ban
     * Disable (ban) a user account.
     */
    @PutMapping("/api/admin/users/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, String>> banUser(@PathVariable Long id) {
        try {
            adminService.banUser(id);
            return ResponseEntity.ok(Map.of("message", "User banned successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/users/{id}/unban
     * Re-enable a banned user account.
     */
    @PutMapping("/api/admin/users/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, String>> unbanUser(@PathVariable Long id) {
        try {
            adminService.unbanUser(id);
            return ResponseEntity.ok(Map.of("message", "User unbanned successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/users/{id}
     * Permanently remove a user account.
     */
    @DeleteMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/stats
     * Return service statistics (user count, recent sign-ups).
     */
    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", adminService.getUserCount(),
                "recentUsers", adminService.getRecentUsers(RECENT_USER_LIMIT)
        ));
    }
}
