package gr.aueb.cf.eduapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

public record ResponseMessageDTO(String code, String description) {

    public ResponseMessageDTO(String code) {
        this(code, "");     // Calls the canonical constructor
    }
}
