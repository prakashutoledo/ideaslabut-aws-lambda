/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
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
import java.util.Objects;
import java.util.Set;

/**
 * A writer that can write data in UTF-8 encoding CSV format
 *
 * @author Prakash Khadka <br>
 *     Created on: Jan 30, 2022
 */
public class CSVWriter implements AutoCloseable, Flushable {
    private static final char UTF8_BOM = '\ufeff';
    private static final String DEFAULT_WRITER_DIRECTORY = "build/elasticsearch";
    private static final String CSV_EXTENSION = "csv";

    /**
     * A builder for {@link CSVWriter}
     */
    public static class Builder {
        private String fileName;
        private String delimiter;
        private Set<String> headers;
        private Path outputDirectory;

        /**
         * Creates a new instance of csv writer {@link Builder}
         */
        private Builder() {
            this.delimiter = ",";
            this.fileName = "temp";
            this.headers = new HashSet<>();
            this.outputDirectory = Path.of(DEFAULT_WRITER_DIRECTORY);
        }

        /**
         * Sets the file name for this csv writer builder
         *
         * @param fileName a file name to set
         *
         * @return a reference to this csv writer builder
         */
        public Builder withFileName(String fileName) {
            this.fileName = requireNonNull(fileName);
            return this;
        }

        public Builder withOutputDirectory(Path directory) {
            if (directory == null) {
                throw new NullPointerException("Directory is null");
            }

            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException("Not a directory");
            }

            this.outputDirectory = directory;
            return this;
        }

        /**
         * Sets the default delimiter for thus csv writer builder
         *
         * @param delimiter a default delimiter to set
         *
         * @return a reference to this csv writer builder
         */
        public Builder withDelimiter(String delimiter) {
            this.delimiter = requireNonNull(delimiter);
            return this;
        }

        /**
         * Sets the csv headers for this csv writer builder
         *
         * @param headers a csv headers to set
         *
         * @return a reference to this csv writer builder
         */
        public Builder withHeaders(Set<String> headers) {
            this.headers = requireNonNull(headers);
            return this;
        }

        /**
         * Builds the new {@link CSVWriter}
         * <p>
         * This will also truncate or recreate the file name provided in this builder and opens that file
         * to print writer as csv writer
         *
         * @return a newly created csv writer
         */
        public CSVWriter build() {
            var bufferedWriterFactory = UncheckedIOFunction.wrap((String fileName) -> {
                Path path = Files.createDirectories(outputDirectory);
                return Files.newBufferedWriter(
                    Path.of(String.format("%s/%s.%s", path.toString(), fileName, CSV_EXTENSION)),
                    UTF_8, CREATE, TRUNCATE_EXISTING, WRITE
                );
            });

            var csvWriter = new CSVWriter(this, bufferedWriterFactory.apply(fileName));
            if (!headers.isEmpty()) {
                csvWriter.writeHeaders();
            }
            return csvWriter;
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
        this.printWriter = printWriter;
        this.headers = builder.headers;
    }

    /**
     * Write the given set of csv headers if it is not empty or not null
     *
     * @param headers a set of csv headers
     *
     * @throws IllegalArgumentException if given headers is null or empty
     */
    public boolean writeHeaders(Set<String> headers) {
        if (headers == null || headers.isEmpty() || !this.headers.isEmpty()) {
            return false;
        }

        this.headers = headers;
        writeHeaders();
        return true;
    }

    /**
     * Writes the given property map with csv value mapped with csv headers
     *
     * @param property a property map with value mapped into csv headers as key
     */
    public void writeRow(Map<String, String> property) {
        if (property == null || property.isEmpty()) {
            throw new IllegalArgumentException("Property cannot be null");
        }
        writeRows(List.of(requireNonNull(property)));
    }

    /**
     * Writes the given list of property map with csv value mapped with csv headers as key
     * If no headers are set for this csv writer it will write the headers from the key set
     * generated by first element from property map assuming that all list as same set of keys
     * <p>
     * It is the callers responsibility to make sure that same sets of keys are used as csv property map
     *
     * @param csvRows a list csv rows with value mapped into csv headers as key
     */
    public void writeRows(List<Map<String, String>> csvRows) {
        if (csvRows == null || csvRows.isEmpty()) {
            throw new IllegalArgumentException("Empty properties provided");
        }

        if (this.headers == null || headers.isEmpty()) {
            writeHeaders(csvRows.get(0).keySet());
        }

        csvRows.stream()
            .map(csvRow -> headers.stream().map(csvRow::get).collect(joining(delimiter)))
            .forEach(this::writeLine);
    }

    /**
     * Write csv headers line with the default delimiter with UTF-8 bom
     */
    private void writeHeaders() {
        write(String.valueOf(UTF8_BOM));
        writeLine(String.join(delimiter, headers));
    }

    /**
     * Writes the given value to the underlying print writer as csv row
     * This will not validate if the given value is valid csv row assuming that it is valid
     * It is always the caller responsibility to pass the valid csv row
     *
     * @param value a value to write
     *
     * @throws UncheckedIOException a wrapper around checked {@link IOException} if any
     */
    private void write(String value) {
        try {
            printWriter.write(value);
        }
        catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    /**
     * Writes the given value (concatenating with extra line separator) as csv row to underlying print writer
     * assuming that given value is valid csv row. It is always the caller responsibility to pass valid csv row
     *
     * @param value a csv value to write
     */
    private void writeLine(String value) {
        write(value + System.lineSeparator());
    }

    /**
     * Flushes the underlining print writer
     *
     * @throws IOException if any
     */
    @Override
    public void flush() throws IOException {
        printWriter.flush();
    }

    /**
     * Closes the underlying csv print writer
     *
     * @throws IOException if any
     */
    @Override
    public void close() throws IOException {
        printWriter.close();
    }
}
