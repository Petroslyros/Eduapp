package gr.aueb.cf.eduapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

public record AuthenticationRequestDTO( @NotNull String username, @NotNull String password) {}
