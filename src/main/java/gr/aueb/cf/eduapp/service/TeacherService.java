package gr.aueb.cf.eduapp.service;

import gr.aueb.cf.eduapp.core.enums.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.eduapp.core.enums.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.eduapp.core.filters.Paginated;
import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
import gr.aueb.cf.eduapp.core.specifications.TeacherSpecification;
import gr.aueb.cf.eduapp.dto.PersonalInfoInsertDTO;
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
public class TeacherService implements ITeacherService{

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final Mapper mapper;


    @Override
    @Transactional(rollbackOn = Exception.class)
    public TeacherReadOnlyDTO saveTeacher(TeacherInsertDTO teacherInsertDTO, MultipartFile amkaFile) throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, IOException {

        if(userRepository.findByVat(teacherInsertDTO.userInsertDTO().vat()).isPresent()) {
            throw new AppObjectAlreadyExists("VAT", "Personal info with VAT " + teacherInsertDTO.userInsertDTO().vat() +
                    "already exists");
        }

        if(userRepository.findByVat(teacherInsertDTO.personalInfoInsertDTO().amka()).isPresent()) {
            throw new AppObjectAlreadyExists("AMKA", "Personal info with AMKA " + teacherInsertDTO.personalInfoInsertDTO().amka() +
                    "already exists");
        }

        if(userRepository.findByVat(teacherInsertDTO.userInsertDTO().username()).isPresent()) {
            throw new AppObjectAlreadyExists("Username", "User info with Username " + teacherInsertDTO.userInsertDTO().username() +
                    "already exists");
        }

        if(personalInfoRepository.findByIdentityNumber(teacherInsertDTO.personalInfoInsertDTO().identityNumber()).isPresent()) {
            throw new AppObjectAlreadyExists("Identity", "User with identity number " + teacherInsertDTO.personalInfoInsertDTO().identityNumber() +
                    "already exists");
        }

        Teacher teacher = mapper.mapToTeacherEntity(teacherInsertDTO);

        if(amkaFile != null && !amkaFile.isEmpty()) {
          //   saveAmkaFIle(teacher.getPersonalInfo(), amkaFile);
        }

        // Saves teacher (cascades to User and PersonalInfo)
        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher with amka={} saved.", teacherInsertDTO.personalInfoInsertDTO().amka());

        return mapper.mapToTeacherReadOnlyDTO(savedTeacher);



    }

    @Override
    public Page<TeacherReadOnlyDTO> getPaginatedTeachers(int page, int size) {
        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page,size, Sort.by(defaultSort).ascending());
        log.debug("Paginated teachers were returned successfully with page={} and size={} ", page, size);

        return teacherRepository.findAll(pageable).map(mapper::mapToTeacherReadOnlyDTO);
    }

    @Override
    public Paginated<TeacherReadOnlyDTO> getTeachersFilteredPaginated(TeacherFilters teacherFilters) {
        var filtered =teacherRepository.findAll(getSpecsFromFilters(teacherFilters), teacherFilters.getPageable());
        log.debug("Filtered and paginated tachers were returned successfully with page={} amd size={}",
                teacherFilters.getPage(),teacherFilters.getPageSize());

        return new Paginated<>(filtered.map(mapper::mapToTeacherReadOnlyDTO));
    }

    private void saveAmkaFile(PersonalInfo personalInfo, MultipartFile amkaFile) throws IOException {
        if(amkaFile == null || !amkaFile.isEmpty()) {

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
            log.info("Attachment for teacher with Amka={} saved", personalInfo.getAmka());
        }
    }
    private String getFileExtension(String filename) {
        if(filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));

        }
        return "";

    }

    private Specification<Teacher> getSpecsFromFilters(TeacherFilters teacherFilters) {
        return TeacherSpecification.trStringFieldLike("uuid", teacherFilters.getUuid())
                .and(TeacherSpecification.teacherUserVatIs(teacherFilters.getUserVat()))
                .and(TeacherSpecification.trPersonalInfoAmkaIs(teacherFilters.getUserAmka()))
                .and(TeacherSpecification.trUserIsActive(teacherFilters.getActive()));
    }
}
