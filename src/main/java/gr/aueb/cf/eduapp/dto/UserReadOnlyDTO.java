package gr.aueb.cf.eduapp.dto;

import lombok.*;

@Builder
public record UserReadOnlyDTO(String firstname, String lastname, String vat) {}
