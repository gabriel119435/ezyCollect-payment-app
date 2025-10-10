package org.example.dto.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserInput(
        @NotNull
        @Size(min = 3, max = 50)
        String username,
        @NotNull
        @Size(min = 10, max = 50)
        String password
) {
}
