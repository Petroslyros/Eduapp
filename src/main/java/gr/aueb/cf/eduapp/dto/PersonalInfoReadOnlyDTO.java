package gr.aueb.cf.eduapp.dto;

import lombok.*;

@Builder
public record PersonalInfoReadOnlyDTO(String amka, String identityNumber) {}
