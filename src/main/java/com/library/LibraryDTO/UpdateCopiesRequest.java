package com.library.LibraryDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UpdateCopiesRequest {

    @Min(value = 0, message = "Total copies cannot be negative")
    @Max(value = 10_000, message = "Total copies cannot exceed 10000")
    private int totalCopies;

    public UpdateCopiesRequest() {}

    public UpdateCopiesRequest(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
}
