package gr.aueb.cf.eduapp.core.filters;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public abstract class GenericFilters {

    // Default settings for pagination and sorting
    private final static int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_SORT_COLUMN = "id";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.ASC;

    // Request parameters (can be overridden by query params)
    private int page;
    private int pageSize;
    private Sort.Direction sortDirection;
    private String sortBy;

    // Ensure pageSize is always valid
    public int getPageSize() {
        return pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    // Ensure page number is not negative
    public int getPage() {
        return Math.max(page, 0);
    }

    // Return default sort direction if not set
    public Sort.Direction getSortDirection(){
        if (this.sortDirection == null) return DEFAULT_SORT_DIRECTION;
        return this.sortDirection;
    }

    // Return default sort column if not set
    public String getSortBy(){
        if (this.sortBy == null || StringUtils.isBlank(this.sortBy)) return DEFAULT_SORT_COLUMN;
        return this.sortBy;
    }

    // Create a Pageable object used in queries (page, size, sort)
    public Pageable getPageable(){
        return PageRequest.of(getPage(), getPageSize(), getSort());
    }

    // Create a Sort object based on direction and column
    public Sort getSort(){
        return Sort.by(this.getSortDirection(), this.getSortBy());
    }
}
