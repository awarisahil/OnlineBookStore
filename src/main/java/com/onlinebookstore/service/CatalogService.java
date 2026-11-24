package com.onlinebookstore.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Category;
import com.onlinebookstore.repository.BookRepository;
import com.onlinebookstore.repository.CategoryRepository;

@Service
public class CatalogService {

    public static final int PAGE_SIZE = 12;

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public CatalogService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public record CatalogFilter(String query, Long categoryId, Boolean inStockOnly, String sort, int page) {
    }

    public record CatalogPage(Page<Book> books, List<Category> categories, CatalogFilter filter) {
    }

    @Transactional(readOnly = true)
    public CatalogPage browse(CatalogFilter filter) {
        Specification<Book> spec = Specification.where(null);
        if (filter.query() != null && !filter.query().isBlank()) {
            String q = filter.query().trim();
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("author")), "%" + q.toLowerCase() + "%")));
        }
        if (filter.categoryId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("category").get("id"), filter.categoryId()));
        }
        if (Boolean.TRUE.equals(filter.inStockOnly())) {
            spec = spec.and((root, cq, cb) -> cb.greaterThan(root.get("quantity"), 0));
        }

        Sort sort = switch (filter.sort() == null ? "" : filter.sort()) {
            case "price-asc" -> Sort.by("price");
            case "price-desc" -> Sort.by("price").descending();
            case "title" -> Sort.by("name");
            case "author" -> Sort.by("author");
            default -> Sort.by("createdAt").descending();
        };

        int page = Math.max(0, filter.page());
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, sort);
        return new CatalogPage(bookRepository.findAll(spec, pageable), categoryRepository.findAll(Sort.by("name")),
                filter);
    }

    @Transactional(readOnly = true)
    public List<Book> suggest(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String q = query.trim();
        List<Book> byName = bookRepository
                .findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(q, q);
        return byName.stream().limit(8).toList();
    }

    @Transactional(readOnly = true)
    public List<Book> bestsellers(int limit) {
        List<Book> sold = bookRepository.findBestsellers(PageRequest.of(0, limit));
        if (sold.size() < limit) {
            List<String> have = sold.stream().map(Book::getBarcode).toList();
            List<Book> fill = new ArrayList<>(sold);
            bookRepository.findTop8ByOrderByCreatedAtDesc().stream()
                    .filter(b -> !have.contains(b.getBarcode()))
                    .limit(limit - sold.size())
                    .forEach(fill::add);
            return fill;
        }
        return sold;
    }

    @Transactional(readOnly = true)
    public List<Book> newArrivals(int limit) {
        return bookRepository.findTop8ByOrderByCreatedAtDesc().stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public List<Book> featured(int limit) {
        List<Book> featured = bookRepository.findTop6ByFeaturedTrueOrderByCreatedAtDesc();
        if (featured.isEmpty()) {
            return bookRepository.findTop8ByOrderByCreatedAtDesc().stream().limit(limit).toList();
        }
        return featured.stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public List<Book> lowStock(int threshold) {
        return bookRepository.findTop6ByQuantityLessThanEqualOrderByQuantityAsc(threshold);
    }

    @Transactional(readOnly = true)
    public List<Book> relatedTo(Book book, int limit) {
        if (book.getCategory() == null) {
            return List.of();
        }
        return bookRepository.findByCategoryId(book.getCategory().getId()).stream()
                .filter(b -> !b.getBarcode().equals(book.getBarcode()))
                .limit(limit)
                .toList();
    }
}
