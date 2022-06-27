/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.sneaky;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Unit test for {@link UncheckedIOFunction#wrap(UncheckedIOFunction)}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
class UncheckedIOFunctionTest {
    @Test
    void wrap() {
        var function = UncheckedIOFunction.wrap(input -> {
            throw new IOException();
        });

        assertThrows(UncheckedIOException.class, () -> function.apply(null));

        var function1 = UncheckedIOFunction.wrap(input -> {
            throw new EOFException();
        });

        assertThrows(UncheckedIOException.class, () -> function1.apply(null));

        var function2 = UncheckedIOFunction.wrap(input -> {
            throw new IndexOutOfBoundsException();
        });

        assertThrows(IndexOutOfBoundsException.class, () -> function2.apply(null));
    }
}
