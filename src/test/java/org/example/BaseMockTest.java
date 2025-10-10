package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public class BaseMockTest {
    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        closeable.close();
    }
}
