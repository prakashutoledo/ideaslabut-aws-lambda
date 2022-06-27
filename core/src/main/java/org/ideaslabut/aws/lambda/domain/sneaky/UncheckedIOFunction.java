/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.sneaky;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * A function which throws checked io exception
 *
 * @param <T> A type of input parameter for the function
 * @param <R> A type of result for the function
 * @param <E> A type of {@link IOException}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
@FunctionalInterface
public interface UncheckedIOFunction<T, R, E extends IOException> {
    /**
     * Wraps the given function to throw unchecked io exceptions if checked io exception is caught
     *
     * @param uncheckedIOFunction a unchecked io function
     * @param <T> a type of input parameter for the function
     * @param <R> a type of the result
     *
     * @return a function without any checked exception
     */
    static <T, R> Function<T, R> wrap(UncheckedIOFunction<T, R, ? super IOException> uncheckedIOFunction) {
        return value -> {
            try {
                return uncheckedIOFunction.apply(value);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        };
    }

    /**
     * Performs the given input operation to get the result
     * i.e. value = f(x);
     *
     * @param input a input to the function
     * @return a result from the operation
     * 
     * @throws E {@link IOException} if any occurs
     */
    R apply(T input) throws E;
}
