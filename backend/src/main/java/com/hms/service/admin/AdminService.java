package com.hms.service.admin;

import com.hms.dto.admin.response.AdminStatsResponse;
import com.hms.dto.admin.response.UserListItemResponse;
import com.hms.dto.auth.response.MessageResponse;
import org.springframework.data.domain.Page;

public interface AdminService {
    AdminStatsResponse getSystemStats();
    Page<UserListItemResponse> getAllUsers(int page, int size);
    MessageResponse toggleUserStatus(Long userId);
}
