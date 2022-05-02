package org.ideaslabut.aws.lambda.service;

import java.io.BufferedWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

public class CSVWriter implements AutoCloseable, Flushable {
    private static final char UTF8_BOM = '\ufeff';
    private static final String CSV_EXTENSION = ".csv";

    private final String delimiter;
    private BufferedWriter printWriter;
    private Set<String> headers;

    public static class Builder {
        private String fileName;
        private String delimiter;

        private Builder() {
            this.delimiter = ",";
            this.fileName = "temp";
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public CSVWriter build() {
            CSVWriter csvWriterService = new CSVWriter(delimiter);
            csvWriterService.open(fileName);
            return csvWriterService;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private CSVWriter(String delimiter) {
        this.delimiter = delimiter;
        this.headers = new HashSet<>();
    }

    private void open(String fileName) {
        try {
            Files.createDirectories(Path.of("build/elasticsearch"));
            printWriter = Files.newBufferedWriter(Path.of(String.format("build/elasticsearch/%s.csv",
                    Objects.requireNonNull(fileName))),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
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
            var firstProperty = properties.get(0);
            writeHeaders(firstProperty.keySet());
        }

        properties.stream().map(map -> headers.stream().map(map::get).collect(Collectors.joining(delimiter))).forEach(this::writeLine);
    }

    private void writeHeaders() {
        write(String.valueOf(UTF8_BOM));
        writeLine(headers.stream().collect(Collectors.joining(delimiter)));
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
}
