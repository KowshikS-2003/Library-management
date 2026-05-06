package com.library.LibraryDTO;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serializes a {@link Long} ID as a zero-padded 6-digit string.
 * Values larger than 6 digits are written as-is (no truncation).
 * {@code null} values are written as JSON {@code null}.
 *
 * Example: {@code 1L -> "000001"}, {@code 123456L -> "123456"}, {@code 1234567L -> "1234567"}.
 */
public class PaddedIdSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(String.format("%06d", value));
    }
}
