package gr.aueb.cf.eduapp.core.filters;

import lombok.*;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TeacherFilters extends GenericFilters {

    // Optional filter for teacher's unique UUID
    @Nullable
    private String uuid;

    // Optional filter for teacher's VAT number (AFM)
    @Nullable
    private String userVat;

    // Optional filter for teacher's AMKA number
    @Nullable
    private String userAmka;

    // Optional filter for whether the teacher is active
    @Nullable
    private Boolean active;
}
