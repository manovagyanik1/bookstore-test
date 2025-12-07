package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:sqlite::memory:",
    "spring.datasource.driver-class-name=org.sqlite.JDBC",
    "spring.sql.init.mode=never"
})
@DisplayName("BookRepository Integration Tests")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Create table for in-memory SQLite
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                isbn TEXT UNIQUE,
                price DECIMAL(10, 2),
                quantity INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
    }

    @Test
    @DisplayName("Should save and retrieve book")
    void shouldSaveAndRetrieveBook() {
        // Given
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("123-456-789");
        book.setPrice(new BigDecimal("19.99"));
        book.setQuantity(10);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());

        // When
        jdbcTemplate.update(
            "INSERT INTO books (title, author, isbn, price, quantity, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            book.getTitle(), book.getAuthor(), book.getIsbn(), book.getPrice(),
            book.getQuantity(), book.getCreatedAt(), book.getUpdatedAt()
        );
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
        book.setId(id);

        Optional<Book> found = bookRepository.findById(id);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Book");
        assertThat(found.get().getAuthor()).isEqualTo("Test Author");
        assertThat(found.get().getIsbn()).isEqualTo("123-456-789");
    }

    @Test
    @DisplayName("Should find all books")
    void shouldFindAllBooks() {
        // Given
        insertTestBook("Book 1", "Author 1", "ISBN-1", new BigDecimal("10.00"), 5);
        insertTestBook("Book 2", "Author 2", "ISBN-2", new BigDecimal("20.00"), 10);

        // When
        List<Book> books = bookRepository.findAll();

        // Then
        assertThat(books).hasSize(2);
    }

    @Test
    @DisplayName("Should find book by ISBN")
    void shouldFindBookByIsbn() {
        // Given
        Long id = insertTestBook("Test Book", "Test Author", "TEST-ISBN-123", 
            new BigDecimal("15.99"), 20);

        // When
        Optional<Book> found = bookRepository.findByIsbn("TEST-ISBN-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getIsbn()).isEqualTo("TEST-ISBN-123");
        assertThat(found.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    @DisplayName("Should find books by author")
    void shouldFindBooksByAuthor() {
        // Given
        insertTestBook("Book 1", "John Doe", "ISBN-1", new BigDecimal("10.00"), 5);
        insertTestBook("Book 2", "John Doe", "ISBN-2", new BigDecimal("20.00"), 10);
        insertTestBook("Book 3", "Jane Smith", "ISBN-3", new BigDecimal("30.00"), 15);

        // When
        List<Book> books = bookRepository.findByAuthor("John Doe");

        // Then
        assertThat(books).hasSize(2);
        assertThat(books).allMatch(book -> "John Doe".equals(book.getAuthor()));
    }

    @Test
    @DisplayName("Should find books by title containing")
    void shouldFindBooksByTitleContaining() {
        // Given
        insertTestBook("The Great Gatsby", "F. Scott Fitzgerald", "ISBN-1", 
            new BigDecimal("12.99"), 50);
        insertTestBook("Gatsby's Legacy", "Another Author", "ISBN-2", 
            new BigDecimal("15.99"), 30);
        insertTestBook("Different Book", "Author", "ISBN-3", 
            new BigDecimal("10.99"), 20);

        // When
        List<Book> books = bookRepository.findByTitleContaining("Gatsby");

        // Then
        assertThat(books).hasSize(2);
        assertThat(books).allMatch(book -> book.getTitle().contains("Gatsby"));
    }

    @Test
    @DisplayName("Should check if book exists by id")
    void shouldCheckIfBookExistsById() {
        // Given
        Long id = insertTestBook("Test Book", "Author", "ISBN-123", 
            new BigDecimal("10.00"), 5);

        // When
        boolean exists = bookRepository.existsById(id);
        boolean notExists = bookRepository.existsById(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should delete book by id")
    void shouldDeleteBookById() {
        // Given
        Long id = insertTestBook("Test Book", "Author", "ISBN-123", 
            new BigDecimal("10.00"), 5);
        assertThat(bookRepository.existsById(id)).isTrue();

        // When
        bookRepository.deleteById(id);

        // Then
        assertThat(bookRepository.existsById(id)).isFalse();
        assertThat(bookRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when finding non-existent book")
    void shouldReturnEmptyWhenFindingNonExistentBook() {
        // When
        Optional<Book> found = bookRepository.findById(999L);
        Optional<Book> foundByIsbn = bookRepository.findByIsbn("NON-EXISTENT");

        // Then
        assertThat(found).isEmpty();
        assertThat(foundByIsbn).isEmpty();
    }

    // Helper method to insert test data
    private Long insertTestBook(String title, String author, String isbn, 
                                BigDecimal price, Integer quantity) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            "INSERT INTO books (title, author, isbn, price, quantity, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            title, author, isbn, price, quantity, now, now
        );
        return jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
    }
}

