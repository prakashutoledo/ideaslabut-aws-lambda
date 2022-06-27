/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import org.ideaslabut.aws.lambda.domain.sneaky.UncheckedIOFunction;

import java.io.Flushable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A writer that can write data in CSV format
 *
 * @author Prakash Khadka <br>
 *     Created on: Jan 30, 2022
 */
public class CSVWriter implements AutoCloseable, Flushable {
    private static final char UTF8_BOM = '\ufeff';
    private static final String DEFAULT_WRITER_DIRECTORY = "build/elasticsearch";
    private static final String CSV_EXTENSION = "csv";

    private final String delimiter;
    private final Writer printWriter;
    private Set<String> headers;

    /**
     * Creates a new instance of {@link CSVWriter}
     *
     * @param builder a builder to use to build this instance
     * @param printWriter a print writer to set
     */
    private CSVWriter(Builder builder, Writer printWriter) {
        this.delimiter = builder.delimiter;
        this.headers = builder.headers;
        this.printWriter = printWriter;
    }

    public void writeHeaders(Set<String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("Empty headers provided");
        }
        this.headers = headers;
        writeHeaders();
    }

    public void write(Map<String, String> property) {
        writeProperties(List.of(property));
    }

    public void writeProperties(List<Map<String, String>> properties) {
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Empty properties provided");
        }

        if (headers.isEmpty()) {
            writeHeaders(properties.get(0).keySet());
        }

        properties.stream()
            .map(map -> headers.stream().map(map::get).collect(joining(delimiter)))
            .forEach(this::writeLine);
    }

    private void writeHeaders() {
        write(String.valueOf(UTF8_BOM));
        writeLine(String.join(delimiter, headers));
    }

    private void write(String value) {
        try {
            printWriter.write(value);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void writeLine(String value) {
        write(value + System.lineSeparator());
    }

    @Override
    public void flush() throws IOException {
        printWriter.flush();
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
    }

    public static class Builder {
        private String fileName;
        private String delimiter;
        private Set<String> headers;

        private Builder() {
            this.delimiter = ",";
            this.fileName = "temp";
            this.headers = new HashSet<>();
        }

        public Builder withFileName(String fileName) {
            this.fileName = requireNonNull(fileName);
            return this;
        }

        public Builder withDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder withHeaders(Set<String> headers) {
            if (headers == null || headers.isEmpty()) {
                throw new IllegalArgumentException("Empty headers provided");
            }

            this.headers = headers;
            return this;
        }

        public CSVWriter build() {
            var bufferedWriterFactory = UncheckedIOFunction.wrap((String fileName) -> {
                Files.createDirectories(Path.of(DEFAULT_WRITER_DIRECTORY));
                return Files.newBufferedWriter(
                    Path.of(
                        String.format("%s/%s.%s",
                            DEFAULT_WRITER_DIRECTORY,
                            fileName,
                            CSV_EXTENSION
                        )
                    ),
                    UTF_8,
                    CREATE,
                    TRUNCATE_EXISTING,
                    WRITE
                );
            });
            return new CSVWriter(this, bufferedWriterFactory.apply(fileName));
        }
    }

    /**
     * Creates a new instance of {@link CSVWriter} builder
     *
     * @return a newly created builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
