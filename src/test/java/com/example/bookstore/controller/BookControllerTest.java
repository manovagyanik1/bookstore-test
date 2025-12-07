package com.example.bookstore.controller;

import com.example.bookstore.model.Book;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("BookController Integration Tests")
class BookControllerTest {

    private MockMvc mockMvc;

    private BookService bookService;

    private Book testBook;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);
        BookController controller = new BookController(bookService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
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
    @DisplayName("GET /api/books - Should return all books")
    void shouldGetAllBooks() throws Exception {
        // Given
        List<Book> books = Arrays.asList(testBook, testBook2);
        when(bookService.getAllBooks()).thenReturn(books);

        // When & Then
        mockMvc.perform(get("/api/books"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].title").value("The Great Gatsby"));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return book when id exists")
    void shouldGetBookByIdWhenExists() throws Exception {
        // Given
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        // When & Then
        mockMvc.perform(get("/api/books/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("The Great Gatsby"));

        verify(bookService, times(1)).getBookById(1L);
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return 404 when id does not exist")
    void shouldReturn404WhenBookIdDoesNotExist() throws Exception {
        // Given
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/books/999"))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBookById(999L);
    }

    @Test
    @DisplayName("POST /api/books - Should create book successfully")
    void shouldCreateBookSuccessfully() throws Exception {
        // Given
        Book createdBook = new Book();
        createdBook.setId(3L);
        createdBook.setTitle("1984");
        createdBook.setAuthor("George Orwell");
        createdBook.setIsbn("978-0-452-28423-4");
        createdBook.setPrice(new BigDecimal("10.99"));
        createdBook.setQuantity(25);

        when(bookService.createBook(any(Book.class))).thenReturn(createdBook);

        // When & Then
        String jsonBody = """
            {
                "title": "1984",
                "author": "George Orwell",
                "isbn": "978-0-452-28423-4",
                "price": 10.99,
                "quantity": 25
            }
            """;
        
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(3L))
            .andExpect(jsonPath("$.title").value("1984"));

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - Should update book successfully")
    void shouldUpdateBookSuccessfully() throws Exception {
        // Given
        Book savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setTitle("Updated Title");
        savedBook.setPrice(new BigDecimal("15.99"));

        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(savedBook);

        // When & Then
        String jsonBody = """
            {
                "title": "Updated Title",
                "author": "F. Scott Fitzgerald",
                "isbn": "978-0-7432-7356-5",
                "price": 15.99,
                "quantity": 75
            }
            """;
        
        mockMvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(bookService, times(1)).updateBook(eq(1L), any(Book.class));
    }

    @Test
    @DisplayName("PATCH /api/books/{id} - Should partially update book successfully")
    void shouldPatchBookSuccessfully() throws Exception {
        // Given
        Book patchedBook = new Book();
        patchedBook.setId(1L);
        patchedBook.setTitle("The Great Gatsby");
        patchedBook.setPrice(new BigDecimal("20.00"));
        patchedBook.setQuantity(100);

        when(bookService.patchBook(eq(1L), any(Book.class))).thenReturn(patchedBook);

        // When & Then
        String jsonBody = """
            {
                "price": 20.00,
                "quantity": 100
            }
            """;
        
        mockMvc.perform(patch("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.price").value(20.00));

        verify(bookService, times(1)).patchBook(eq(1L), any(Book.class));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should delete book successfully")
    void shouldDeleteBookSuccessfully() throws Exception {
        // Given
        doNothing().when(bookService).deleteBook(1L);

        // When & Then
        mockMvc.perform(delete("/api/books/1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }
}
