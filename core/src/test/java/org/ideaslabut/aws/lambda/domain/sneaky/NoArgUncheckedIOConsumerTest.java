/*
 * Copyright 2022 IDEAS Lab @ UT. All rights reserved.
 */
package org.ideaslabut.aws.lambda.domain.sneaky;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Unit test for {@link NoArgUncheckedIOConsumer#wrap(NoArgUncheckedIOConsumer)}
 *
 * @author Prakash Khadka <br>
 *     Created On: Jun 27, 2022
 */
class NoArgUncheckedIOConsumerTest {
    private static class ChildIOException extends IOException {
    }

    @Test
    void wrap() {
        var consumer = NoArgUncheckedIOConsumer.wrap(() -> {
            throw new IOException();
        });

        assertThrows(UncheckedIOException.class, consumer::accept);

        consumer = NoArgUncheckedIOConsumer.wrap(() -> {
            throw new ChildIOException();
        });
        assertThrows(UncheckedIOException.class, consumer::accept);

        consumer = NoArgUncheckedIOConsumer.wrap(() -> {
            throw new RuntimeException();
        });

        assertThrows(RuntimeException.class, consumer::accept);
    }
}
