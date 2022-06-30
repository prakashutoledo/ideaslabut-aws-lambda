/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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
            Arguments.of(Set.of("a", "b", "c"), ",", "file1"),
            Arguments.of(Set.of("a", "b", "c", "d"), ";", "file2")
        );
    }

    @TempDir
    private Path tempPath;

    private CSVWriter.Builder builder;

    @BeforeEach
    void setup() {
        builder = CSVWriter.builder().withOutputDirectory(tempPath);
    }

    @Test
    void validCSVBuilderTest() throws IOException {
        assertNotNull(builder, "Builder should be created");
        try (var csvWriter = builder.withHeaders(Set.of("id, name"))
                .withDelimiter(",").withFileName("test").build()) {
            assertNotNull(csvWriter, "Writer shouldn't be null");
        }
    }

    @Test
    void close() throws IOException {
        var csvWriter = builder.build();
        csvWriter.close();

        var exception = assertThrows(UncheckedIOException.class, () -> csvWriter.writeHeaders(Set.of("a")));
        assertEquals("Stream closed", exception.getCause().getMessage());

        assertDoesNotThrow(csvWriter::close);
    }

    @Test
    void flush() throws IOException {
        try(var csvWriter = builder.withHeaders(Set.of("a")).build()) {
            var bytes = Files.readAllBytes(Path.of(tempPath.toString(), "temp.csv"));

            assertEquals(0, bytes.length, "No header bytes were written as bytes were buffered");
            csvWriter.flush();

            assertHeaders("temp", ",", Set.of("a"));
        }
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
    void writeValidHeaders(Set<String> headers, String delimiter, String fileName) throws IOException {
        try (var csvWriter = builder.withDelimiter(delimiter).withFileName(fileName).build()) {
            assertTrue(csvWriter.writeHeaders(headers), "Headers is written successfully");
            csvWriter.flush();

            assertHeaders(fileName, delimiter, headers);

            assertFalse(csvWriter.writeHeaders(headers), "Headers cannot be rewritten");
        }
    }

    @Test
    void writeInValidHeaders() throws IOException {
        try (var csvWriter = builder.withHeaders(Set.of("header1")).build()) {
            assertFalse(csvWriter.writeHeaders(null), "Null headers cannot be written");
            assertFalse(csvWriter.writeHeaders(Set.of()), "Empty headers cannot be written");
            assertFalse(csvWriter.writeHeaders(Set.of("header2")), "Headers is already written during builder");
        }
    }

    @Test
    void writeValidRow() throws IOException {
        try (var csvWriter = builder.withHeaders(Set.of("header1", "header2")).withFileName("name").build()) {
            csvWriter.writeRow(Map.of("header1", "value1", "header2", "value2"));
            csvWriter.flush();

            assertRow("name", ",", List.of(Set.of("value1", "value2")));
        }
    }

    @Test
    void writeInvalidRow() throws IOException {
        try (var csvWriter = builder.withDelimiter(",").withFileName("fake-name").build()) {
            assertThrows(IllegalArgumentException.class, () -> csvWriter.writeRow(null));
            assertThrows(IllegalArgumentException.class, () -> csvWriter.writeRow(Map.of()));
        }
    }

    @Test
    void writeValidRows() throws IOException {
        try (var csvWriter = builder.withFileName("rows").withDelimiter(";").build()) {
            csvWriter.writeRows(
                List.of(
                    Map.of("first", "value1", "second", "value2"),
                    Map.of("first", "value11", "second", "value22"),
                    Map.of("first", "value111", "second", "value222")
                )
            );
            csvWriter.flush();

            assertRow(
                "rows",
                ";",
                List.of(Set.of("value1", "value2"), Set.of("value11", "value22"), Set.of("value111", "value222"))
            );
        }
    }

    @Test
    void writeInvalidRows() throws IOException {
        try (var csvWriter = builder.build()) {
            assertThrows(IllegalArgumentException.class, () -> csvWriter.writeRows(null));
            assertThrows(IllegalArgumentException.class, () -> csvWriter.writeRows(List.of()));
        }
    }

    private void assertHeaders(String fileName, String delimiter, Set<String> expectedHeaders) throws IOException {
        var readBytes = Files.readAllBytes(Path.of(tempPath.toString(), fileName + ".csv"));
        var bom = new byte[3];
        var actualHeadersBytes = new byte[readBytes.length - 3];

        System.arraycopy(readBytes, 0, bom, 0, 3);
        System.arraycopy(readBytes, 3, actualHeadersBytes, 0, actualHeadersBytes.length);

        assertArrayEquals("\ufeff".getBytes(UTF_8), bom, "Byte order mark");
        assertRow(delimiter, expectedHeaders, new String(actualHeadersBytes), "CSV headers");
    }

    private void assertRow(String fileName, String delimiter, List<Set<String>> expectedRows) throws IOException {
        var lines = Files.readAllLines(Path.of(tempPath.toString(), fileName + ".csv"));

        assertNotNull(lines, "Lines shouldn't be null");
        assertEquals(expectedRows.size() + 1, lines.size(), "Lines size");

        for (int i = 0; i < expectedRows.size(); i++) {
            assertRow(delimiter, expectedRows.get(i), lines.get(i + 1), "Csv row should match");
        }
    }

    private void assertRow(String delimiter, Set<String> expectedRow, String actualRow, String message) {
        assertEquals(expectedRow, Arrays.stream(actualRow.trim().split(delimiter)).collect(toSet()), message);
    }
}
