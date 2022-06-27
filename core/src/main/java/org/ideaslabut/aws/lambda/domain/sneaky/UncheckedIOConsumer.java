/*
 * Copyright 2022 IDEAS Lab @ University of Toledo.. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.sneaky;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * A functional interface with checked {@link IOException}
 *
 * @param <T> A type of parameter to perform operation
 * @param <E> A type of exception extending {@link IOException}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
@FunctionalInterface
public interface UncheckedIOConsumer<T, E extends IOException> {
    /**
     * Returns a wrapped consumer that performs operation on given unchecked io consumer catching
     * io exception and rethrowing unchecked io exception
     *
     * @param uncheckedIOConsumer a unchecked io consumer to perform operation
     * @param <T> A type of parameter accepted by the consumer
     *
     * @return A wrapped consumer throwing unchecked io exception
     */
    static <T> Consumer<T> wrap(final UncheckedIOConsumer<T, ? super IOException> uncheckedIOConsumer) {
        requireNonNull(uncheckedIOConsumer, "Consumer shouldn't be null");
        return value -> {
            try {
                uncheckedIOConsumer.accept(value);
            } catch (final IOException exception) {
                throw new UncheckedIOException(exception);
            }
        };
    }

    /**
     * Performs the operation on given value
     *
     * @param value A value to perform operation on
     *
     * @throws E IOException
     */
    void accept(final T value) throws E;
}
