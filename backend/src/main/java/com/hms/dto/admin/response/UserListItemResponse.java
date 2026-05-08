package com.hms.dto.admin.response;

import com.hms.entity.user.Role;
import com.hms.entity.user.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserListItemResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
