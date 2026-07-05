package com.paper.reviewer.settings;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService service;
    public SettingsController(SettingsService service) { this.service = service; }
    @GetMapping public ApiResponse<SettingsResponse> get(@AuthenticationPrincipal AuthenticatedUser user) { return ApiResponse.success(service.get(user.userId())); }
    @PutMapping public ApiResponse<SettingsResponse> update(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody UpdateSettingsRequest request) { return ApiResponse.success(service.update(user.userId(), request)); }
}
