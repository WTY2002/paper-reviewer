package com.paper.reviewer.paper.controller;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.service.PaperService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaperControllerTest {
    private final PaperService service = mock(PaperService.class);
    private final PaperController controller = new PaperController(service);
    private final AuthenticatedUser user = new AuthenticatedUser(42L, "owner@example.com");

    @Test
    void listUsesAuthenticatedUserId() {
        Paper paper = new Paper(5L, 42L, "Title", "paper.pdf", "path", 12, 1,
                "en", "EXTRACTED", LocalDateTime.now(), LocalDateTime.now());
        when(service.list(42L)).thenReturn(List.of(paper));

        var response = controller.list(user);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).extracting(item -> item.paperId()).containsExactly(5L);
        verify(service).list(42L);
    }

}
