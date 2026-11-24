package com.onlinebookstore.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onlinebookstore.model.Book;

public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {

    List<Book> findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(String name, String author);

    /**
     * Atomically decrease stock, guarding against overselling concurrent buyers.
     * Returns the number of rows updated - 0 means not enough stock.
     */
    @Modifying
    @Query("UPDATE Book b SET b.quantity = b.quantity - :qty "
            + "WHERE b.barcode = :barcode AND b.quantity >= :qty")
    int decrementStock(@Param("barcode") String barcode, @Param("qty") int qty);

    /** Atomically add stock back (restock / cancelled orders). */
    @Modifying
    @Query("UPDATE Book b SET b.quantity = b.quantity + :qty WHERE b.barcode = :barcode")
    int incrementStock(@Param("barcode") String barcode, @Param("qty") int qty);

    @EntityGraph(attributePaths = { "category" })
    Page<Book> findAll(Pageable pageable);

    List<Book> findTop6ByFeaturedTrueOrderByCreatedAtDesc();

    List<Book> findTop8ByOrderByCreatedAtDesc();

    List<Book> findTop6ByQuantityLessThanEqualOrderByQuantityAsc(int threshold);

    @Query("SELECT b FROM Book b JOIN b.category c WHERE c.id = :categoryId")
    List<Book> findByCategoryId(@Param("categoryId") Long categoryId);

    /** Bestsellers: books with the most copies sold, across non-cancelled orders. */
    @Query("SELECT b FROM Book b JOIN OrderItem i ON i.barcode = b.barcode "
            + "JOIN ShopOrder o ON i.order = o "
            + "WHERE o.status <> com.onlinebookstore.model.OrderStatus.CANCELLED "
            + "GROUP BY b ORDER BY SUM(i.quantity) DESC")
    List<Book> findBestsellers(Pageable pageable);
}
