package com.paper.reviewer.dashboard;
import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.common.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service; public DashboardController(DashboardService service){this.service=service;}
    @GetMapping public ApiResponse<DashboardResponse> get(@AuthenticationPrincipal AuthenticatedUser user){return ApiResponse.success(service.get(user.userId()));}
}
