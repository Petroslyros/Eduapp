package gr.aueb.cf.eduapp.service;

import gr.aueb.cf.eduapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.eduapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.eduapp.core.filters.Paginated;
import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
import gr.aueb.cf.eduapp.core.specifications.TeacherSpecification;
import gr.aueb.cf.eduapp.dto.TeacherInsertDTO;
import gr.aueb.cf.eduapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.eduapp.mapper.Mapper;
import gr.aueb.cf.eduapp.model.Attachment;
import gr.aueb.cf.eduapp.model.PersonalInfo;
import gr.aueb.cf.eduapp.model.Teacher;
import gr.aueb.cf.eduapp.repository.PersonalInfoRepository;
import gr.aueb.cf.eduapp.repository.TeacherRepository;
import gr.aueb.cf.eduapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherService implements ITeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final Mapper mapper;

    /**
     * Saves a new Teacher with validation and optional file upload.
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public TeacherReadOnlyDTO saveTeacher(TeacherInsertDTO teacherInsertDTO, MultipartFile amkaFile)
            throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, IOException {

        // Check for VAT duplication
        if (userRepository.findByVat(teacherInsertDTO.userInsertDTO().vat()).isPresent()) {
            throw new AppObjectAlreadyExists("VAT", "Personal info with VAT already exists");
        }

        // Check for AMKA duplication
        if (userRepository.findByVat(teacherInsertDTO.personalInfoInsertDTO().amka()).isPresent()) {
            throw new AppObjectAlreadyExists("AMKA", "Personal info with AMKA already exists");
        }

        // Check for username duplication
        if (userRepository.findByVat(teacherInsertDTO.userInsertDTO().username()).isPresent()) {
            throw new AppObjectAlreadyExists("Username", "Username already exists");
        }

        // Check for identity number duplication
        if (personalInfoRepository.findByIdentityNumber(teacherInsertDTO.personalInfoInsertDTO().identityNumber()).isPresent()) {
            throw new AppObjectAlreadyExists("Identity", "User with identity number already exists");
        }

        // Convert DTO to Entity
        Teacher teacher = mapper.mapToTeacherEntity(teacherInsertDTO);

        // Save AMKA file if provided
        if (amkaFile != null && !amkaFile.isEmpty()) {
            // saveAmkaFile(teacher.getPersonalInfo(), amkaFile); // Uncomment if file upload logic is needed
        }

        // Save to DB
        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher with amka={} saved.", teacherInsertDTO.personalInfoInsertDTO().amka());

        // Convert back to DTO to return
        return mapper.mapToTeacherReadOnlyDTO(savedTeacher);
    }

    /**
     * Returns paginated teachers (without filtering).
     */
    @Override
    public Page<TeacherReadOnlyDTO> getPaginatedTeachers(int page, int size) {
        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());

        log.debug("Paginated teachers returned with page={} and size={}", page, size);

        return teacherRepository.findAll(pageable)
                .map(mapper::mapToTeacherReadOnlyDTO);
    }

    /**
     * Returns filtered + paginated teachers based on filter criteria.
     */
    @Override
    public Paginated<TeacherReadOnlyDTO> getTeachersFilteredPaginated(TeacherFilters teacherFilters) {
        var filtered = teacherRepository.findAll(getSpecsFromFilters(teacherFilters), teacherFilters.getPageable());

        log.debug("Filtered teachers returned with page={} and size={}", teacherFilters.getPage(), teacherFilters.getPageSize());

        return new Paginated<>(filtered.map(mapper::mapToTeacherReadOnlyDTO));
    }

    /**
     * Saves the uploaded AMKA file to the disk and associates it with the teacher's personal info.
     */
    private void saveAmkaFile(PersonalInfo personalInfo, MultipartFile amkaFile) throws IOException {
        if (amkaFile == null || !amkaFile.isEmpty()) {
            String originalFileName = amkaFile.getOriginalFilename();
            String savedName = UUID.randomUUID().toString() + getFileExtension(originalFileName);
            String uploadDirectory = "uploads/";
            Path filePath = Paths.get(uploadDirectory + savedName);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, amkaFile.getBytes());

            Attachment attachment = new Attachment();
            attachment.setFilename(originalFileName);
            attachment.setSavedName(savedName);
            attachment.setFilePath(filePath.toString());
            attachment.setContentType(amkaFile.getContentType());
            attachment.setExtension(getFileExtension(originalFileName));

            personalInfo.setAmkaFile(attachment);
            log.info("Attachment for teacher with AMKA={} saved", personalInfo.getAmka());
        }
    }

    /**
     * Extracts file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    /**
     * Creates Specification object to be used in filtered queries.
     */
    private Specification<Teacher> getSpecsFromFilters(TeacherFilters teacherFilters) {
        return TeacherSpecification.trStringFieldLike("uuid", teacherFilters.getUuid())
                .and(TeacherSpecification.teacherUserVatIs(teacherFilters.getUserVat()))
                .and(TeacherSpecification.trPersonalInfoAmkaIs(teacherFilters.getUserAmka()))
                .and(TeacherSpecification.trUserIsActive(teacherFilters.getActive()));
    }
}
