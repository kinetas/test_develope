package com.trpg.controller;

import com.trpg.dto.share.SharedContentSummary;
import com.trpg.repository.UserRepository;
import com.trpg.service.SharedContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * TRPG 공유 페이지 뷰 컨트롤러.
 * Thymeleaf 템플릿을 서빙하고, 서버사이드 모델 데이터를 제공합니다.
 */
@Slf4j
@Controller
@RequestMapping("/share")
@RequiredArgsConstructor
public class ShareViewController {

    private final SharedContentService sharedContentService;
    private final UserRepository userRepository;

    /**
     * GET /share
     * 공유 콘텐츠 목록 페이지.
     */
    @GetMapping
    public String sharePage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        List<SharedContentSummary> allContents = sharedContentService.getAll();
        List<SharedContentSummary> popularContents = sharedContentService.getPopular();

        model.addAttribute("allContents", allContents);
        model.addAttribute("popularContents", popularContents);
        model.addAttribute("activePage", "share");

        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername())
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }

        log.debug("공유 목록 페이지 렌더링: allContents={}, popular={}",
                allContents.size(), popularContents.size());
        return "share-list";
    }

    /**
     * GET /share/new
     * 새 공유 등록 페이지.
     */
    @GetMapping("/new")
    public String shareCreatePage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute("activePage", "share");

        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername())
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }

        return "share-create";
    }
}
