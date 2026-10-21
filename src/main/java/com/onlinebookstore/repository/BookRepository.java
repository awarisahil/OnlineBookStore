package com.onlinebookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onlinebookstore.model.Book;

public interface BookRepository extends JpaRepository<Book, String> {

    /**
     * Atomically decrease stock, guarding against overselling concurrent buyers.
     * Returns the number of rows updated - 0 means not enough stock.
     */
    @Modifying
    @Query("UPDATE Book b SET b.quantity = b.quantity - :qty "
            + "WHERE b.barcode = :barcode AND b.quantity >= :qty")
    int decrementStock(@Param("barcode") String barcode, @Param("qty") int qty);
}
