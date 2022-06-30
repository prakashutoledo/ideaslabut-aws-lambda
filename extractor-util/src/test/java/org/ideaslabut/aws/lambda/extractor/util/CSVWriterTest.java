/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Unit test for {@link CSVWriter}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 29, 2022
 */
class CSVWriterTest {
    private static Stream<Arguments> csvHeadersArguments() {
        return Stream.of(
            Arguments.of(Set.of("a", "b", "c"), ","),
            Arguments.of(Set.of("a", "b", "c", "d"), ";")
        );
    }

    @TempDir
    private Path path;

    private CSVWriter.Builder builder;

    @BeforeEach
    void setup() {
        builder = CSVWriter.builder().withOutputDirectory(path);
    }

    @Test
    void validCSVBuilderTest() {
        assertNotNull(builder, "Builder should be created");

        var csvWriter = builder.withHeaders(Set.of("id, name"))
            .withDelimiter(",").withFileName("test").build();
        assertNotNull(csvWriter, "Writer shouldn't be null");
    }

    @Test
    void invalidCSVBuilderTest() throws IOException {
        assertThrows(NullPointerException.class, () -> CSVWriter.builder().withFileName(null));
        assertThrows(NullPointerException.class, () -> CSVWriter.builder().withDelimiter(null));
        assertThrows(NullPointerException.class, () -> CSVWriter.builder().withHeaders(null));
        assertThrows(NullPointerException.class, () -> CSVWriter.builder().withOutputDirectory(null));

        final Path tempFilePath = Files.createTempFile("test", ".text");
        assertThrows(IllegalArgumentException.class, () -> CSVWriter.builder().withOutputDirectory(tempFilePath));
        Files.deleteIfExists(tempFilePath);
    }

    @ParameterizedTest
    @MethodSource("csvHeadersArguments")
    void writeValidHeaders(Set<String> headers, String delimiter) throws IOException {
        var csvWriter = builder.withDelimiter(delimiter).build();

        assertTrue(csvWriter.writeHeaders(headers), "Headers is written successfully");
        csvWriter.flush();

        var bytes = Files.readAllBytes(Path.of(path.toString(), "temp.csv"));
        var bom = new byte[3];
        var actualBytes = new byte[bytes.length - 3];
        System.arraycopy(bytes, 0, bom, 0, 3);
        System.arraycopy(bytes, 3, actualBytes, 0, actualBytes.length);

        assertArrayEquals("\ufeff".getBytes(UTF_8), bom, "Byte order mark");
        assertEquals(
            headers,
            Arrays.stream(new String(actualBytes).trim().split(delimiter)).collect(toSet()),
            "CSV headers"
        );

        assertFalse(csvWriter.writeHeaders(headers), "Headers cannot be rewritten");
    }

    @Test
    void writeInValidHeaders() {
        var csvWriter = builder.withHeaders(Set.of("header1")).build();
        assertFalse(csvWriter.writeHeaders(null), "Null headers cannot be written");
        assertFalse(csvWriter.writeHeaders(Set.of()), "Empty headers cannot be written");
        assertFalse(csvWriter.writeHeaders(Set.of("header2")), "Headers is already written during builder");
    }

    @Test
    void writeValidRow() throws IOException {
        var csvWriter = builder
            .withHeaders(Set.of("header1", "header2"))
            .withDelimiter(",").withFileName("fake-name")
            .build();
        csvWriter.writeRow(Map.of("header1", "value1", "header2", "value2"));
        csvWriter.flush();

        var lines = Files.readAllLines(Path.of(path.toString(), "fake-name.csv"));
        assertNotNull(lines, "Lines shouldn't be null");
        assertEquals(2, lines.size(), "Lines size");

        var csvRow = lines.get(1);
        assertEquals(
            Set.of("value1", "value2"),
            Arrays.stream(csvRow.trim().split(",")).collect(toSet()),
            "Csv row should match"
        );
    }

    @Test
    void writeInvalidRow() {
        var csvWriter = builder.withDelimiter(",").withFileName("fake-name").build();
        assertThrows(IllegalArgumentException.class, () -> csvWriter.writeRow(null));
        assertThrows(IllegalArgumentException.class, () -> csvWriter.writeRow(Map.of()));
    }
}
