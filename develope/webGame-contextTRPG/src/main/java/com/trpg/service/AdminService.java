package com.trpg.service;

import com.trpg.dto.admin.UserSummaryDTO;
import com.trpg.model.User;
import com.trpg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    /**
     * Retrieve a summary list of all registered users.
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * Disable (ban) a user account by setting enabled = false.
     */
    @Transactional
    public void banUser(Long userId) {
        User user = findUserById(userId);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Admin banned user: id={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * Re-enable (unban) a previously banned user account.
     */
    @Transactional
    public void unbanUser(Long userId) {
        User user = findUserById(userId);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Admin unbanned user: id={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * Permanently delete a user account.
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
        log.info("Admin deleted user: id={}, username={}", userId, user.getUsername());
    }

    /**
     * Return the total number of registered users.
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Return the most recently registered users up to {@code limit} entries.
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getRecentUsers(int limit) {
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userId));
    }

    private UserSummaryDTO toSummary(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
