package com.paper.reviewer.paper.controller;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.service.PaperService;
import com.paper.reviewer.history.HistoryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaperControllerTest {
    private final PaperService service = mock(PaperService.class);
    private final HistoryService history = mock(HistoryService.class);
    private final PaperController controller = new PaperController(service, history);
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

    @Test
    void detailAndDeleteUseAuthenticatedUserId() {
        Paper paper = new Paper(5L, 42L, "Title", "paper.pdf", "path", 12, 1,
                "en", "EXTRACTED", LocalDateTime.now(), LocalDateTime.now());
        when(service.get(42L, 5L)).thenReturn(paper);

        assertThat(controller.detail(user, 5L).data().paperId()).isEqualTo(5L);
        controller.delete(user, 5L);

        verify(service).get(42L, 5L);
        verify(history).deletePaper(42L, 5L);
    }
}
