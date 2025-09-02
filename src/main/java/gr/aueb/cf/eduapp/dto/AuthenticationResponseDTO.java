package gr.aueb.cf.eduapp.dto;

import lombok.*;

public record AuthenticationResponseDTO(String firstname, String lastname, String token) {}
