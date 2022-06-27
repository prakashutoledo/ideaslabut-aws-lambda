/*
 * Copyright 2022 IDEAS Lab @ University of Toledo.. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.sneaky;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A no argument consumer with checked io exception
 *
 * @param <E> A parameter of type {@link IOException}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
@FunctionalInterface
public interface NoArgUncheckedIOConsumer<E extends IOException> {
    /**
     * Returns a wrapped consumer that performs operation on given no argument consumer catching
     * checked {@link IOException} and rethrowing {@link UncheckedIOException}
     *
     * @param noArgConsumer A no argument consumer to perform operation on
     *
     * @return a wrapped consumer
     */
    static NoArgConsumer wrap(NoArgUncheckedIOConsumer<? super IOException> noArgConsumer) {
        requireNonNull(noArgConsumer, "Consumer cannot be null");
        return () -> {
            try {
                noArgConsumer.accept();
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        };
    }

    /**
     * Performs the operation
     *
     * @throws E An exception of type {@link IOException}
     */
    void accept() throws IOException;
}
