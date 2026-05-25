package com.trpg.dto.admin;

import com.trpg.model.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private boolean enabled;
    private LocalDateTime createdAt;
}
