/*
 * Copyright 2022 IDEAS Lab @ University of Toledo. All rights reserved.
 */
package org.ideaslabut.aws.lambda.extractor.util;

import static java.lang.System.currentTimeMillis;
import static org.ideaslabut.aws.lambda.extractor.util.FormatterUtil.formattedMillis;

import java.io.PrintStream;
import java.util.Objects;

/**
 * A command line progress bar
 *
 * @author Prakash Khadka <br>
 *     Created on: Jan 30, 2022
 */
public class ProgressBar {
    private static final String STATUS_PIPE = "|";
    private static final String STATUS_MINUS = "-";
    private static final String STATUS_DONE = "done...";

    /**
     * A builder for {@link ProgressBar}
     */
    public static class Builder {
        private int maxStep;
        private long totalElement;
        private char delimiter;
        private PrintStream printStream;
        private String prefix;

        /**
         * Creates a new instance of progress bar {@link Builder}
         */
        private Builder() {
            this.printStream = System.out;
            this.delimiter = '#';
            this.maxStep = 25;
        }

        /**
         * A max step to set for this builder
         *
         * @param maxStep a max step to set
         *
         * @return a reference of this builder
         */
        public Builder withMaxStep(int maxStep) {
            this.maxStep = validateMaxStep(maxStep);
            return this;
        }

        /**
         * A total element to set for this builder
         *
         * @param totalElement a total element to set
         *
         * @return a reference of this builder
         */
        public Builder withTotalElement(long totalElement) {
            this.totalElement = validateTotalElement(totalElement);
            return this;
        }

        /**
         * A delimiter to set for this builder
         *
         * @param delimiter a delimiter to set
         *
         * @return a reference to this builder
         */
        public Builder withDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * A print stream to set for this builder
         *
         * @param printStream a print stream to set
         *
         * @return a reference to this builder
         */
        public Builder withPrintStream(PrintStream printStream) {
            this.printStream = Objects.requireNonNull(printStream);
            return this;
        }

        /**
         * A prefix to set for this progress builder
         *
         * @param prefix a prefix to set
         *
         * @return a reference to this builder
         */
        public Builder withPrefix(String prefix) {
            this.prefix = prefix == null ? "" : prefix;
            return this;
        }

        /**
         * Builds a new progress bar from this builder
         *
         * @return a newly created progress bar
         */
        public ProgressBar build() {
            return new ProgressBar(this);
        }

        /**
         * Validates given max step and returns if it is valid
         *
         * @param maxStep a max step to validates
         *
         * @return a validated max step
         *
         * @throws IllegalArgumentException if max step is negative or greater than 100
         */
        private int validateMaxStep(int maxStep) {
            if (maxStep > 0 && maxStep <= 100) {
                return maxStep;
            }

            throw new IllegalArgumentException("Max step should be between 0 and 100 inclusive");
        }

        /**
         * Validates given total element and returns if it is valid
         *
         * @param totalElement a total element to be validated
         *
         * @return a validated total element
         *
         * @throws IllegalArgumentException if total element is negative or zero
         */
        private long validateTotalElement(long totalElement) {
            if (totalElement > 0) {
                return totalElement;
            }

            throw new IllegalArgumentException("Total element should be positive number greater than 0");
        }
    }

    /**
     * Creates a new builder for {@link ProgressBar}
     *
     * @return a newly created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private final int maxStep;
    private final char delimiter;
    private final PrintStream printStream;
    private final String format;
    private final StringBuilder delimiterBuilder;
    private final long totalElement;
    private long startTime;
    private long currentElementSize;
    private String status;

    /**
     * Creates a new instance of {@link ProgressBar}
     *
     * @param builder a builder to set
     */
    private ProgressBar(Builder builder) {
        this.delimiter = builder.delimiter;
        this.printStream = builder.printStream;
        this.maxStep = builder.maxStep;
        this.totalElement = builder.totalElement;

        this.format = "\r" + builder.prefix + " [%-" + this.maxStep + "s]" + " [%" + ("" + totalElement).length() + "d/" + totalElement + "] [%3d%%] [%s] [%s]";
        this.delimiterBuilder = new StringBuilder(this.maxStep);
        this.currentElementSize = 0;
        this.status = STATUS_PIPE;
    }

    /**
     * Initializes start time for this progress bar by using current time millis from system
     */
    public void initStartTime() {
        initStartTime(currentTimeMillis());
    }

    /**
     * Sets the given millis as start time for this progress bar
     *
     * @param millis a millis to set
     */
    public void initStartTime(long millis) {
        this.startTime = millis;
    }

    /**
     * Updates the progress bar by adding given element count to current element count
     *
     * @param element a element count to add to current element count
     */
    public void updateBy(long element) {
        updateTo(currentElementSize + element);
    }

    /**
     * Updates the current element count for this progress bar with given element count
     * If such element count is greater than total element count for this progress bar then
     * current element count is set to max element count even if given count is greater than current
     * element count
     *
     * @param element an element count to set as current element count
     */
    public void updateTo(long element) {
        if (element > totalElement) {
            element = totalElement;
        }

        currentElementSize = element;
        update();
    }

    /**
     * Updates the progress bar by calculating percentage based on current element count
     */
    private void update() {
        int progressPercentage = Long.valueOf((currentElementSize * 100) / totalElement).intValue();
        int remainingDelimiter = Long.valueOf(
            (maxStep * currentElementSize) / totalElement - delimiterBuilder.length()
        ).intValue();

        if (remainingDelimiter > 0) {
            char[] delimiters = new char[remainingDelimiter];
            for (int i = 0; i < remainingDelimiter; i++) {
                delimiters[i] = delimiter;
            }
            delimiterBuilder.append(delimiters);
        }

        if (currentElementSize == totalElement) {
            status = STATUS_DONE;
        }

        printStream.printf(
            format,
            delimiterBuilder.toString(),
            currentElementSize,
            progressPercentage,
            formattedMillis(currentTimeMillis() - startTime),
            status
        );

        status = STATUS_PIPE.equals(status) ? STATUS_MINUS : STATUS_PIPE;
    }
}
