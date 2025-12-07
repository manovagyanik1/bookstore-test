package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("The Great Gatsby");
        testBook.setAuthor("F. Scott Fitzgerald");
        testBook.setIsbn("978-0-7432-7356-5");
        testBook.setPrice(new BigDecimal("12.99"));
        testBook.setQuantity(50);
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());

        testBook2 = new Book();
        testBook2.setId(2L);
        testBook2.setTitle("To Kill a Mockingbird");
        testBook2.setAuthor("Harper Lee");
        testBook2.setIsbn("978-0-06-112008-4");
        testBook2.setPrice(new BigDecimal("14.99"));
        testBook2.setQuantity(30);
    }

    @Test
    @DisplayName("Should return all books when getAllBooks is called")
    void shouldReturnAllBooks() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook, testBook2);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // When
        List<Book> result = bookService.getAllBooks();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testBook, testBook2);
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return book when getBookById is called with valid id")
    void shouldReturnBookWhenIdExists() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.getBookById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBook);
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTitle()).isEqualTo("The Great Gatsby");
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when getBookById is called with non-existent id")
    void shouldReturnEmptyWhenIdDoesNotExist() {
        // Given
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Book> result = bookService.getBookById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should return book when getBookByIsbn is called with valid isbn")
    void shouldReturnBookWhenIsbnExists() {
        // Given
        when(bookRepository.findByIsbn("978-0-7432-7356-5")).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.getBookByIsbn("978-0-7432-7356-5");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo("978-0-7432-7356-5");
        verify(bookRepository, times(1)).findByIsbn("978-0-7432-7356-5");
    }

    @Test
    @DisplayName("Should return books when getBooksByAuthor is called")
    void shouldReturnBooksByAuthor() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByAuthor("F. Scott Fitzgerald")).thenReturn(expectedBooks);

        // When
        List<Book> result = bookService.getBooksByAuthor("F. Scott Fitzgerald");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("F. Scott Fitzgerald");
        verify(bookRepository, times(1)).findByAuthor("F. Scott Fitzgerald");
    }

    @Test
    @DisplayName("Should return books when searchBooksByTitle is called")
    void shouldReturnBooksByTitleSearch() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByTitleContaining("Gatsby")).thenReturn(expectedBooks);

        // When
        List<Book> result = bookService.searchBooksByTitle("Gatsby");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Gatsby");
        verify(bookRepository, times(1)).findByTitleContaining("Gatsby");
    }

    @Test
    @DisplayName("Should create book successfully when isbn does not exist")
    void shouldCreateBookWhenIsbnDoesNotExist() {
        // Given
        Book newBook = new Book();
        newBook.setTitle("1984");
        newBook.setAuthor("George Orwell");
        newBook.setIsbn("978-0-452-28423-4");
        newBook.setPrice(new BigDecimal("10.99"));
        newBook.setQuantity(25);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(jdbcTemplate.getJdbcOperations()).thenReturn(mock(org.springframework.jdbc.core.JdbcTemplate.class));
        when(jdbcTemplate.getJdbcOperations().queryForObject(anyString(), eq(Long.class))).thenReturn(1L);
        when(jdbcTemplate.update(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class))).thenReturn(1);

        // When
        Book result = bookService.createBook(newBook);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("1984");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(bookRepository, times(1)).findByIsbn(newBook.getIsbn());
        verify(jdbcTemplate, times(1)).update(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class));
    }

    @Test
    @DisplayName("Should throw exception when creating book with duplicate isbn")
    void shouldThrowExceptionWhenCreatingBookWithDuplicateIsbn() {
        // Given
        Book newBook = new Book();
        newBook.setIsbn("978-0-7432-7356-5");

        when(bookRepository.findByIsbn("978-0-7432-7356-5")).thenReturn(Optional.of(testBook));

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(newBook))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
        verify(bookRepository, times(1)).findByIsbn(newBook.getIsbn());
        verify(jdbcTemplate, never()).update(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class));
    }

    @Test
    @DisplayName("Should update book successfully when id exists")
    void shouldUpdateBookWhenIdExists() {
        // Given
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setPrice(new BigDecimal("15.99"));
        updatedDetails.setQuantity(100);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book result = bookService.updateBook(1L, updatedDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("15.99"));
        assertThat(result.getQuantity()).isEqualTo(100);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent book")
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {
        // Given
        Book updatedDetails = new Book();
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.updateBook(999L, updatedDetails))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
        verify(bookRepository, times(1)).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when updating book with duplicate isbn")
    void shouldThrowExceptionWhenUpdatingBookWithDuplicateIsbn() {
        // Given
        Book updatedDetails = new Book();
        updatedDetails.setIsbn("978-0-06-112008-4"); // Different ISBN that exists

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findByIsbn("978-0-06-112008-4")).thenReturn(Optional.of(testBook2));

        // When & Then
        assertThatThrownBy(() -> bookService.updateBook(1L, updatedDetails))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
        verify(jdbcTemplate, never()).update(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class));
    }

    @Test
    @DisplayName("Should patch book successfully when id exists")
    void shouldPatchBookWhenIdExists() {
        // Given
        Book patchDetails = new Book();
        patchDetails.setPrice(new BigDecimal("20.00"));
        // Only price is set, other fields are null

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book result = bookService.patchBook(1L, patchDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getTitle()).isEqualTo("The Great Gatsby"); // Original title preserved
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should delete book successfully when id exists")
    void shouldDeleteBookWhenIdExists() {
        // Given
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        // When
        bookService.deleteBook(1L);

        // Then
        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent book")
    void shouldThrowExceptionWhenDeletingNonExistentBook() {
        // Given
        when(bookRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> bookService.deleteBook(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
        verify(bookRepository, times(1)).existsById(999L);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should return true when book exists")
    void shouldReturnTrueWhenBookExists() {
        // Given
        when(bookRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = bookService.bookExists(1L);

        // Then
        assertThat(result).isTrue();
        verify(bookRepository, times(1)).existsById(1L);
    }
}

