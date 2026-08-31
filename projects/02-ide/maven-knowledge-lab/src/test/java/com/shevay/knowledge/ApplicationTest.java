package com.shevay.knowledge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationTest {

    @Test
    @DisplayName("Should execute Application main method cleanly without throwing exceptions")
    void testMainExecution() {
        assertDoesNotThrow(() -> Application.main(new String[]{}));
    }
}
