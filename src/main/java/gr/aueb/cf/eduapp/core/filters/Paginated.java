package gr.aueb.cf.eduapp.core.filters;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class Paginated<T> {

    // The actual data of the current page
    List<T> data;

    // Total number of elements in the full result (across all pages)
    long totalElements;

    // Total number of pages
    int totalPages;

    // Number of elements in the current page
    int numberOfElements;

    // Index of the current page (zero-based)
    int currentPage;

    // Size of the page (number of items per page)
    int pageSize;

    // Constructor that initializes everything from a Page object
    public Paginated(Page<T> page) {
        this.data = page.getContent();                // Actual page data
        this.totalElements = page.getTotalElements(); // How many total items
        this.totalPages = page.getTotalPages();       // How many total pages
        this.numberOfElements = page.getNumberOfElements(); // Items in current page
        this.currentPage = page.getNumber();          // Current page index
        this.pageSize = page.getSize();               // Page size
    }
}
