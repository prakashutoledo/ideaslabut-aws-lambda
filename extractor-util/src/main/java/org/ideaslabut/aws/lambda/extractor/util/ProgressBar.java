package org.ideaslabut.aws.lambda.extractor.util;

import java.io.PrintStream;
import java.util.Objects;
import java.util.stream.IntStream;

public class ProgressBar {
    public static class Builder {
        private int maxStep;
        private long totalElement;
        private String delimiter;
        private PrintStream printStream;
        private String prefix;

        private Builder() {
            this.printStream = System.out;
            this.delimiter = "#";
            this.maxStep = 25;
        }

        public Builder withMaxStep(int maxStep) {
            this.maxStep = maxStep;
            return this;
        }

        public Builder withTotalElement(long totalElement) {
            this.totalElement = totalElement;
            return this;
        }

        public Builder withDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder withPrintStream(PrintStream printStream) {
            this.printStream = printStream;
            return this;
        }

        public Builder withPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public ProgressBar build() {
            return new ProgressBar(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private int maxStep;
    private long totalElement;
    private String delimiter;
    private PrintStream printStream;
    private final String format;
    private final StringBuilder delimiterBuilder;
    private long startTime;
    private long currentElementSize;

    private ProgressBar(Builder builder) {
        this(builder.maxStep, builder.totalElement, builder.delimiter, builder.printStream, builder.prefix);
    }

    private ProgressBar(int maxStep, long totalElement, String delimiter, PrintStream printStream, String prefix) {
        this.maxStep = maxStep;
        this.totalElement = totalElement;
        this.delimiter = delimiter;
        this.printStream = Objects.requireNonNull(printStream, "Print stream should not be null");
        this.maxStep = validateMaxStep(maxStep);
        this.totalElement = validateTotalElement(totalElement);
        this.format = (prefix == null ? "" : prefix) + " [%-" + this.maxStep + "s]" + " [%" +
                String.valueOf(totalElement).length() + "d/" +
                totalElement + "] [%3d%%] [%s]\r";
        this.delimiterBuilder = new StringBuilder();
        this.currentElementSize = 0;
    }

    private int validateMaxStep(int maxStep) {
        if (maxStep > 0 && maxStep <= 100) {
            return maxStep;
        }

        throw new IllegalArgumentException("Max step should be between 0 and 100 inclusive");
    }

    private long validateTotalElement(long totalElement) {
        if (totalElement > 0) {
            return totalElement;
        }

        throw new IllegalArgumentException("Total element should be positive number greater than 0");
    }


    public void initStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public void setTotalElement(long totalElement) {
        this.totalElement = validateTotalElement(totalElement);
    }

    public void initStartTime(long millis) {
        this.startTime = millis;
    }

    public void updateBy(long element) {
        if (element > totalElement) {
            throw new IllegalArgumentException("Element cannot exceed total element number");
        }
        if (currentElementSize + element > totalElement) {
            currentElementSize = totalElement;
        }
        else {
            currentElementSize += element;
        }

        update();
    }

    public void updateTo(long element) {
        if (element > totalElement) {
            element = totalElement;
        }

        currentElementSize = element;
        update();
    }

    private void update() {
        long endTime = System.currentTimeMillis();
        int percentage = Long.valueOf((currentElementSize * 100) / totalElement).intValue();
        int remainingDelimiter = Long.valueOf((maxStep * currentElementSize) / totalElement - delimiterBuilder.length()).intValue();

        if (remainingDelimiter > 0) {
            IntStream.range(0, remainingDelimiter).mapToObj(i -> "#").forEach(delimiterBuilder::append);
        }

        printStream.printf(format, delimiterBuilder.toString(), currentElementSize, percentage, formattedMillis(endTime - startTime));
    }

    private static String formattedMillis(long millis) {
        int secondToMillis = 1000;
        int minuteToMillis = secondToMillis * 60;
        int hourToMillis = minuteToMillis * 60;

        long hrs = millis / hourToMillis;
        millis = millis % hourToMillis;
        long minutes = millis / minuteToMillis;
        millis = millis % minuteToMillis;
        long seconds = millis / secondToMillis;
        millis = millis % secondToMillis;

        return String.format("%02d:%02d:%02d:%03d", hrs, minutes, seconds, millis);
    }

    public int getMaxStep() {
        return maxStep;
    }

    public long getTotalElement() {
        return totalElement;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }
}
