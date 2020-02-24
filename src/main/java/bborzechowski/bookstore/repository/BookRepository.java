package bborzechowski.bookstore.repository;

import bborzechowski.bookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query(value = "SELECT * FROM books WHERE id like (?1%)", nativeQuery = true)
    Book findBookByIdd(Long id);

}
