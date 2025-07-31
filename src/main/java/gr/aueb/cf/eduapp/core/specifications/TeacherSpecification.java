package gr.aueb.cf.eduapp.core.specifications;

import gr.aueb.cf.eduapp.model.PersonalInfo;
import gr.aueb.cf.eduapp.model.Teacher;
import gr.aueb.cf.eduapp.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class used to build dynamic JPA Specifications for filtering Teacher entities.
 */
public class TeacherSpecification {

    // Private constructor to prevent instantiation, as this is a utility class
    private TeacherSpecification() {}

    /**
     * Builds a Specification to filter teachers by their user's VAT number.
     * If VAT is null or blank, it returns a dummy always-true condition.
     */
    public static Specification<Teacher> teacherUserVatIs(String vat) {
        return (root, query, criteriaBuilder) -> {
            if (vat == null || vat.isBlank())
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // Always true condition

            // Join the related User entity
            Join<Teacher, User> user = root.join("user");

            // Filter where user.vat = vat
            return criteriaBuilder.equal(user.get("vat"), vat);
        };
    }

    /**
     * Builds a Specification to filter teachers by whether their related user is active.
     * If the value is null, it returns a dummy always-true condition.
     */
    public static Specification<Teacher> trUserIsActive(Boolean isActive) {
        return (root, query, builder) -> {
            if (isActive == null) {
                return builder.isTrue(builder.literal(true)); // Always true condition
            }

            // Join the related User entity
            Join<Teacher, User> user = root.join("user");

            // Filter where user.isActive = isActive
            return builder.equal(user.get("isActive"), isActive);
        };
    }

    /**
     * Builds a Specification to filter teachers by the AMKA field of their related PersonalInfo.
     * If AMKA is null or blank, it returns a dummy always-true condition.
     */
    public static Specification<Teacher> trPersonalInfoAmkaIs(String amka) {
        return (root, query, builder) -> {
            if (amka == null || amka.isBlank()) {
                return builder.isTrue(builder.literal(true)); // Always true condition
            }

            // Join the related PersonalInfo entity
            Join<Teacher, PersonalInfo> personalInfo = root.join("personalInfo");

            // Filter where personalInfo.amka = amka
            return builder.equal(personalInfo.get("amka"), amka);
        };
    }

    /**
     * Generic method to filter using SQL 'LIKE' for partial and case-insensitive string matches.
     * It can be used for any string field on the Teacher entity.
     * Returns always-true condition if value is empty or null.
     */
    public static Specification<Teacher> trStringFieldLike(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty())
                return builder.isTrue(builder.literal(true)); // Always true condition

            // Perform case-insensitive LIKE search on specified field
            return builder.like(
                    builder.upper(root.get(field)),
                    "%" + value.toUpperCase() + "%"
            );
        };
    }
}
