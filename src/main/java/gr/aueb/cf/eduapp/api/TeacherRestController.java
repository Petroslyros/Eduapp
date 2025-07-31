package gr.aueb.cf.eduapp.api;

import gr.aueb.cf.eduapp.core.enums.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.eduapp.core.enums.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.eduapp.core.enums.exceptions.ValidationException;
import gr.aueb.cf.eduapp.core.filters.Paginated;
import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
import gr.aueb.cf.eduapp.dto.TeacherInsertDTO;
import gr.aueb.cf.eduapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.eduapp.service.ITeacherService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeacherRestController {

    private final ITeacherService teacherService;

    /**
     * POST /api/teachers
     * Saves a new teacher from a multipart request (includes JSON + optional AMKA file).
     */
    @PostMapping("/teachers")
    public ResponseEntity<TeacherReadOnlyDTO> saveTeacher(
            @Valid @RequestPart(name = "teacher") TeacherInsertDTO teacherInsertDTO,
            @Nullable @RequestPart(value = "amkaFile", required = false) MultipartFile amkaFile,
            BindingResult bindingResult
    ) throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, IOException, ValidationException {

        // Validate request
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        // Save teacher
        TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.saveTeacher(teacherInsertDTO, amkaFile);

        // Generate URI for the new resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(teacherReadOnlyDTO.getId())
                .toUri();

        // Return 201 Created + resource
        return ResponseEntity.created(location).body(teacherReadOnlyDTO);
    }

    /**
     * GET /api/teachers
     * Returns a page of teachers without any filters.
     */
    @GetMapping("/teachers")
    public ResponseEntity<Page<TeacherReadOnlyDTO>> getPaginatedTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<TeacherReadOnlyDTO> teachersPage = teacherService.getPaginatedTeachers(page, size);
        return ResponseEntity.ok(teachersPage);
    }

    /**
     * POST /api/teachers/filter
     * Returns filtered + paginated teachers based on filter body.
     */
    public ResponseEntity<Paginated<TeacherReadOnlyDTO>> getFilteredAndPaginatedTeachers(
            @Nullable @RequestBody TeacherFilters filters) {

        if (filters == null) filters = TeacherFilters.builder().build();

        Paginated<TeacherReadOnlyDTO> dtoPaginated = teacherService.getTeachersFilteredPaginated(filters);
        return ResponseEntity.ok(dtoPaginated);
    }
}



