package com.example.demo.config;

import com.example.demo.entity.Cafe;
import com.example.demo.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalModelAttributes {

    private final CurrentUserService currentUserService;

    @ModelAttribute("currentCafe")
    public Cafe currentCafe() {
        try {
            return currentUserService.getCurrentCafe();
        } catch (Exception e) {
            log.debug("Could not resolve current cafe: {}", e.getMessage());
            return null;
        }
    }
}
