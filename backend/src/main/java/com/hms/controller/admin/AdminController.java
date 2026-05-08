package com.hms.controller.admin;

import com.hms.dto.admin.response.AdminStatsResponse;
import com.hms.dto.admin.response.UserListItemResponse;
import com.hms.service.admin.AdminService;
import com.hms.dto.auth.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserListItemResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<MessageResponse> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }
}
