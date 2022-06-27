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
 * Unit test for {@link UncheckedIOConsumer#wrap(UncheckedIOConsumer)}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
class UncheckedIOConsumerTest {

    @Test
    void wrap() {
        var consumer = UncheckedIOConsumer.wrap(value -> {
            throw new IOException();
        });

        assertThrows(UncheckedIOException.class, () -> consumer.accept(null));

        var consumer1 = UncheckedIOConsumer.wrap(value -> {
            throw new EOFException();
        });

        assertThrows(UncheckedIOException.class, () -> consumer1.accept(null));


        var consumer2 = UncheckedIOConsumer.wrap(value -> {
            throw new RuntimeException();
        });

        assertThrows(RuntimeException.class, () -> consumer2.accept(null));
    }
}
