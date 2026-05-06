package com.library.LibraryDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Pattern(
            regexp = "^[\\p{L}\\p{N}\\s\\-_:,.'&()!?]+$",
            message = "Title contains invalid characters"
    )
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 2, max = 100, message = "Author must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[\\p{L}\\s.'\\-,]+$",
            message = "Author may only contain letters, spaces, dots, hyphens, commas and apostrophes"
    )
    private String author;

    @Min(value = 1, message = "Total copies must be at least 1")
    @Max(value = 10_000, message = "Total copies cannot exceed 10000")
    private int totalCopies;

    public BookRequest() {}

    public BookRequest(String title, String author, int totalCopies) {
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
}
