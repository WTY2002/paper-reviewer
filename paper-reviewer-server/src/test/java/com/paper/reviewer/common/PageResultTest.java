package com.paper.reviewer.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {
    @Test
    void calculatesTotalPagesAndDefensivelyCopiesContent() {
        List<String> source = new ArrayList<>(List.of("paper"));
        PageResult<String> result = PageResult.of(source, 21, 0, 10);
        source.clear();

        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.content()).containsExactly("paper");
        assertThatThrownBy(() -> result.content().add("other"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
